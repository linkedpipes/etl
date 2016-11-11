package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import net.sf.saxon.s9api.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public final class Xslt implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(Xslt.class);

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "Parameters", optional = true)
    public SingleGraphDataUnit parametersRdf;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public XsltConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final Processor processor = new Processor(false);
        processor.registerExtensionFunction(UUIDGenerator.getInstance());

        final XsltCompiler compiler = processor.newXsltCompiler();
        final XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(
                    new StringReader(configuration.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw exceptionFactory.failure(
                    "Can't compile template.", ex);
        }
        // Load name mapping from input to output.
        final Map<String, String> nameMapping = new HashMap<>();
        parametersRdf.execute((connection) -> {
            final String strQuery = createNamesQuery();
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, strQuery);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(parametersRdf.getGraph());
            query.setDataset(dataset);
            final TupleQueryResult result = query.evaluate();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                nameMapping.put(binding.getValue("fileName").stringValue(),
                        binding.getValue("outputName").stringValue());
            }
        });
        //
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing: {}", entry.getFileName());
            final File inputFile = entry.toFile();
            // Prepare output name.
            final File outputFile;
            if (nameMapping.containsKey(entry.getFileName())) {
                outputFile = outputFiles.createFile(
                        nameMapping.get(entry.getFileName())).toFile();
            } else {
                outputFile = outputFiles.createFile(addExtension(
                        entry.getFileName(),
                        configuration.getNewExtension())).toFile();
            }
            // Prepare transformer.
            final XsltTransformer transformer = executable.load();
            if (parametersRdf != null) {
                LOG.debug("Reading parameters.");
                parametersRdf.execute((connection) -> {
                    final String strQuery = createParametersQuery(entry.getFileName());
                    final TupleQuery query = connection.prepareTupleQuery(
                            QueryLanguage.SPARQL, strQuery);
                    final SimpleDataset dataset = new SimpleDataset();
                    dataset.addDefaultGraph(parametersRdf.getGraph());
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
                        transformer.setParameter(new QName(name),
                                new XdmAtomicValue(value));
                    }
                });
            }
            // Transform
            LOG.debug("Transforming ...");
            boolean deleteFile = false;
            final Serializer output = new Serializer(outputFile);
            try {
                transformer.setSource(new StreamSource(inputFile));
                transformer.setDestination(output);
                transformer.transform();
            } catch (SaxonApiException ex) {
                if (configuration.isSkipOnError()) {
                    LOG.error("Can't transform file: {}",
                            entry.getFileName(), ex);
                    deleteFile = true;
                } else {
                    throw exceptionFactory.failure(
                            "Can't transform file.", ex);
                }
            } finally {
                // Clear document cache.
                try {
                    output.close();
                } catch (SaxonApiException ex) {
                    LOG.warn("Can't close output.", ex);
                }
                transformer.getUnderlyingController().clearDocumentPool();
                try {
                    transformer.close();
                } catch (SaxonApiException ex) {
                    LOG.warn("Can't close transformer.", ex);
                }
            }
            // If the transformation fail, we may end up with incomplete
            // output file. So we need to delete it after the output
            // streams are closed.
            if (deleteFile) {
                outputFile.delete();
            }
            //
            progressReport.entryProcessed();
        }
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
        if (extension == null) {
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
                + "    ?config a <http://etl.linkedpipes.com/ontology/components/t-xslt/Config> ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileInfo> ?fileInfo .\n"
                + "        \n"
                + "    ?fileInfo a <http://etl.linkedpipes.com/ontology/components/t-xslt/FileInfo> ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileName> ?fileName ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/outputName> ?outputName .\n"
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
                + "    ?config a <http://etl.linkedpipes.com/ontology/components/t-xslt/Config> ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileInfo> ?fileInfo .\n"
                + "        \n"
                + "    ?fileInfo a <http://etl.linkedpipes.com/ontology/components/t-xslt/FileInfo> ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/fileName> \""
                + fileName
                + "\" ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameter> ?parameter .\n"
                + "        \n"
                + "    ?parameter a <http://etl.linkedpipes.com/ontology/components/t-xslt/Parameter> ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameterValue> ?value ;\n"
                + "        <http://etl.linkedpipes.com/ontology/components/t-xslt/parameterName> ?name .\n"
                + "}";
    }

}
