package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import java.io.File;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class Xslt implements SimpleExecution {

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

    @Override
    public void execute(Component.Context context)
            throws Component.ExecutionFailed, SystemDataUnitException,
            SesameDataUnit.RepositoryActionFailed {
        final Processor processor = new Processor(false);
        processor.registerExtensionFunction(UUIDGenerator.getInstance());

        final XsltCompiler compiler = processor.newXsltCompiler();
        final XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(
                    new StringReader(configuration.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw new Component.ExecutionFailed(
                    "Can't compile template.", ex);
        }
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing: {}", entry.getFileName());
            final File inputFile = entry.toFile();
            final File outputFile = outputFiles.createFile(addExtension(
                    entry.getFileName(),
                    configuration.getNewExtension())).toFile();

            if (context.canceled()) {
                throw new Component.ExecutionCancelled();
            }
            // Prepare transformer.
            final XsltTransformer transformer = executable.load();
            if (parametersRdf != null) {
                LOG.debug("Reading parameters.");
                parametersRdf.execute((connection) -> {
                    final String strQuery = createQuery(entry.getFileName());
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
            final Serializer output = new Serializer(outputFile);
            try {
                transformer.setSource(new StreamSource(inputFile));
                transformer.setDestination(output);
                transformer.transform();
            } catch (SaxonApiException ex) {
                throw new Component.ExecutionFailed(
                        "Can't transform file.", ex);
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

    private static String createQuery(String fileName) {
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
