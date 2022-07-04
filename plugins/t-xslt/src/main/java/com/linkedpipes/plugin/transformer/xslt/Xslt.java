package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import net.sf.saxon.s9api.SaxonApiException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Xslt implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Xslt.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "Parameters")
    public SingleGraphDataUnit parametersRdf;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public XsltConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        // Load name mapping from input to output.
        final Map<String, String> nameMapping = new HashMap<>();
        parametersRdf.execute((connection) -> {
            final String strQuery = createNamesQuery();
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, strQuery);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(parametersRdf.getReadGraph());
            query.setDataset(dataset);
            final TupleQueryResult result = query.evaluate();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                nameMapping.put(binding.getValue("fileName").stringValue(),
                        binding.getValue("outputName").stringValue());
            }
        });
        // Prepare
        final ConcurrentLinkedQueue<XsltWorker.Payload> workQueue =
                new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<Exception> exceptions =
                new ConcurrentLinkedQueue<>();
        for (FilesDataUnit.Entry entry : inputFiles) {
            final XsltWorker.Payload payload = new XsltWorker.Payload();
            payload.entry = entry;
            // Prepare output name.
            final File outputFile;
            if (nameMapping.containsKey(entry.getFileName())) {
                outputFile = outputFiles.createFile(
                        nameMapping.get(entry.getFileName()));
                nameMapping.get(entry.getFileName());
            } else {
                payload.output = outputFiles.createFile(addExtension(
                        entry.getFileName(),
                        configuration.getNewExtension()));
            }
            // Prepare transformer.

            if (parametersRdf != null) {
                LOG.debug("Reading parameters.");
                parametersRdf.execute((connection) -> {
                    final String strQuery =
                            createParametersQuery(entry.getFileName());
                    final TupleQuery query = connection.prepareTupleQuery(
                            QueryLanguage.SPARQL, strQuery);
                    final SimpleDataset dataset = new SimpleDataset();
                    dataset.addDefaultGraph(parametersRdf.getReadGraph());
                    query.setDataset(dataset);
                    final TupleQueryResult result = query.evaluate();
                    while (result.hasNext()) {
                        final BindingSet binding = result.next();
                        final String name
                                = binding.getValue("name").stringValue();
                        final String value
                                = binding.getValue("value").stringValue();
                        //
                        LOG.debug("Parameter: {} = {}", name, value);
                        //
                        payload.parameter.add(
                                new XsltWorker.Parameter(name, value));

                    }
                });
            }
            workQueue.add(payload);
        }
        // Execute.
        long size = inputFiles.size();
        progressReport.start(size);
        final ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getThreads());
        final AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < configuration.getThreads(); ++i) {
            XsltWorker worker = new XsltWorker(workQueue, exceptions, counter,
                    configuration.isSkipOnError(), size, progressReport);
            try {
                worker.initialize(configuration.getXsltTemplate());
            } catch (SaxonApiException ex) {
                throw new LpException("Can't initialize XSLT.", ex);
            }
            executor.submit(worker);
        }
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
        // Check exceptions.
        if (!exceptions.isEmpty()) {
            int errorCounter = 0;
            for (Exception exception : exceptions) {
                ++errorCounter;
                LOG.error("Can't transform file.", exception);
            }
            LOG.info("Transformed {}/{}", errorCounter, size);
            if (!configuration.isSkipOnError()) {
                throw new LpException("Can't transform all files.");
            }
        } else {
            LOG.info("Transformed {}/{}", size, size);
        }
        //
        progressReport.done();
    }

    /**
     * If given extension is null return original file name. Else if given file
     * has no extension (not dot in the name) extension is added. Else if there
     * is an extension (there is a dot) the string after dot is replaced
     * with given extension.
     *
     * @param fileName
     * @param extension
     * @return
     */
    private static String addExtension(String fileName, String extension) {
        if (extension == null || extension.isEmpty()) {
            return fileName;
        } else {
            return fileName + "." + extension;
        }
    }

    /**
     * Return query that can be used to map input files to their output
     * names.
     *
     * If outputName is set is should be used, in such case the extension is ignored.
     *
     * @return
     */
    private static String createNamesQuery() {
        return ""
                + "SELECT ?fileName ?outputName WHERE {\n"
                +
                "    ?config a <http://etl.linkedpipes.com/ontology/components/t-xslt/Config> ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileInfo> ?fileInfo .\n"
                + "        \n"
                +
                "    ?fileInfo a <http://etl.linkedpipes.com/ontology/components/t-xslt/FileInfo> ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileName> ?fileName ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/outputName> ?outputName .\n"
                + "}";
    }

    /**
     * Return query that can be used to get configuration parameters
     * for file of given name.
     *
     * @param fileName
     * @return
     */
    private static String createParametersQuery(String fileName) {
        return ""
                + "SELECT ?name ?value WHERE {\n"
                +
                "    ?config a <http://etl.linkedpipes.com/ontology/components/t-xslt/Config> ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileInfo> ?fileInfo .\n"
                + "        \n"
                +
                "    ?fileInfo a <http://etl.linkedpipes.com/ontology/components/t-xslt/FileInfo> ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileName> \""
                + fileName
                + "\" ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameter> ?parameter .\n"
                + "        \n"
                +
                "    ?parameter a <http://etl.linkedpipes.com/ontology/components/t-xslt/Parameter> ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameterValue> ?value ;\n"
                +
                "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameterName> ?name .\n"
                + "}";
    }

}
