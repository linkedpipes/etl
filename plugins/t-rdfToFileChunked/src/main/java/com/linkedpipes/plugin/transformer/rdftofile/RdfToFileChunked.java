package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

public final class RdfToFileChunked implements Component, SequentialExecution {

    private static final String FILE_ENCODE = "UTF-8";

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.OutputPort(iri = "OutputFile")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToFileConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private RDFFormat outputFormat;

    private File outputFile;

    @Override
    public void execute() throws LpException {
        prepareOutputFormat();
        prepareOutputFile();
        export();
    }

    private void prepareOutputFormat() throws LpException {
        Optional<RDFFormat> rdfFormat = Rio.getParserFormatForMIMEType(
                configuration.getFileType());
        if (!rdfFormat.isPresent()) {
            throw exceptionFactory.failure("Invalid output file type: {}",
                    configuration.getFileName());
        }
        outputFormat = rdfFormat.get();
    }

    private void prepareOutputFile() throws LpException {
        outputFile = outputFiles.createFile(configuration.getFileName());
    }

    private void export() throws LpException {
        reportStart();
        try (FileOutputStream outStream = new FileOutputStream(outputFile);
             OutputStreamWriter outWriter = new OutputStreamWriter(
                     outStream, Charset.forName(FILE_ENCODE))) {
            RDFWriter writer = createWriter(outWriter);
            writer.startRDF();
            writePrefixes(writer);
            exportChunks(writer);
            writer.endRDF();
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't write data.", ex);
        }
        reportEnd();
    }

    private void exportChunks(RDFWriter writer) throws LpException {
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            for (Statement statement : chunk.toCollection()) {
                writer.handleStatement(statement);
            }
            progressReport.entryProcessed();
        }
    }

    private void reportStart() {
        progressReport.start(inputRdf.size());
    }

    private void reportEnd() {
        progressReport.done();
    }

    private RDFWriter createWriter(OutputStreamWriter streamWriter) {
        RDFWriter writer = Rio.createWriter(outputFormat, streamWriter);

        if (outputFormat.supportsContexts()) {
            writer = new ChangeContext(writer, getOutputGraph());
        }

        return writer;
    }

    private IRI getOutputGraph() {
        if (configuration.getGraphUri() == null) {
            return null;
        }
        return SimpleValueFactory.getInstance().createIRI(
                configuration.getGraphUri());
    }

    private void writePrefixes(RDFWriter writer)
            throws LpException {
        if (configuration.getPrefixes() == null) {
            return;
        }
        final RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        final InputStream stream;
        try {
            stream = new ByteArrayInputStream(
                    configuration.getPrefixes().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("Unsupported encoding exception.",
                    ex);
        }
        try {
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleNamespace(String prefix, String uri)
                        throws RDFHandlerException {
                    writer.handleNamespace(prefix, uri);
                }
            });
            parser.parse(stream, "http://localhost/base");
        } catch (IOException ex) {
            throw exceptionFactory.failure(
                    "Can't read prefixes.", ex);
        }

    }


}
