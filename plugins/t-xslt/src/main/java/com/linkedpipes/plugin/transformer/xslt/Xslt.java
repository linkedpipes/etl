package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
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

/**
 *
 * @author Škoda Petr
 */
public final class Xslt implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Xslt.class);

    @DataProcessingUnit.InputPort(id = "FilesInput")
    public FilesDataUnit inputFiles;

    @DataProcessingUnit.InputPort(id = "Parameters", optional = true)
    public SingleGraphDataUnit parametersRdf;

    @DataProcessingUnit.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @DataProcessingUnit.Configuration
    public XsltConfiguration configuration;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @Override
    public void execute(DataProcessingUnit.Context context)
            throws DataProcessingUnit.ExecutionFailed, SystemDataUnitException,
            SesameDataUnit.RepositoryActionFailed {
        final Processor processor = new Processor(false);
        processor.registerExtensionFunction(UUIDGenerator.getInstance());

        final XsltCompiler compiler = processor.newXsltCompiler();
        final XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(new StringReader(configuration.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw new DataProcessingUnit.ExecutionFailed("Can't compile template.", ex);
        }

        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing: {}", entry.getFileName());
            final File inputFile = entry.getPath();
            final File outputFile = outputFiles.createFile(replaceExtension(entry.getFileName(),
                    configuration.getNewExtension()));

            if (context.canceled()) {
                throw new DataProcessingUnit.ExecutionCancelled();
            }
            // Prepare transformer.
            final XsltTransformer transformer = executable.load();
            if (parametersRdf != null) {
                parametersRdf.execute((connection) -> {
                    final TupleQuery query = connection.prepareTupleQuery(
                            QueryLanguage.SPARQL,
                            createQuery(entry.getFileName()));
                    final SimpleDataset dataset = new SimpleDataset();
                    dataset.addDefaultGraph(parametersRdf.getGraph());
                    query.setDataset(dataset);
                    final TupleQueryResult result = query.evaluate();
                    while (result.hasNext()) {
                        final BindingSet binding = result.next();
                        transformer.setParameter(
                                new QName(binding.getValue("name")
                                        .stringValue()),
                                new XdmAtomicValue(binding.getValue("value")
                                        .stringValue()));
                    }
                });
            }
            // Transform
            final Serializer output = new Serializer(outputFile);
            try {
                transformer.setSource(new StreamSource(inputFile));
                transformer.setDestination(output);
                transformer.transform();
            } catch (SaxonApiException ex) {
                throw new DataProcessingUnit.ExecutionFailed("Can't transform file.", ex);
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
    private static String replaceExtension(String fileName, String extension) {
        if (extension == null) {
            return fileName;
        }
        final int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName + "." + extension;
        } else {
            return fileName.substring(0, dotIndex + 1) + extension;
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
