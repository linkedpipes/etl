package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Chunked version of RdfToFile.
 */
public final class RdfToFileChunked implements Component, SequentialExecution {

    private static final String FILE_ENCODE = "UTF-8";

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

    @Override
    public void execute() throws LpException {
        Optional<RDFFormat> rdfFormat = Rio.getParserFormatForMIMEType(
                configuration.getFileType());
        if (!rdfFormat.isPresent()) {
            throw exceptionFactory.failure("Invalid output file type: {}",
                    configuration.getFileName());
        }
        final File outputFile = outputFiles.createFile(
                configuration.getFileName());
        try (FileOutputStream outStream = new FileOutputStream(outputFile);
             OutputStreamWriter outWriter = new OutputStreamWriter(
                     outStream, Charset.forName(FILE_ENCODE))) {
            RDFWriter writer = Rio.createWriter(rdfFormat.get(), outWriter);
            if (rdfFormat.get().supportsContexts()) {
                writer = new RdfWriterContextRenamer(writer,
                        SimpleValueFactory.getInstance().createIRI(
                                configuration.getGraphUri()));
            }
            writer = new RdfWriterContext(writer);
            writer.startRDF();
            // Add prefixes.
            if (configuration.getPrefixes() != null &&
                    !configuration.getPrefixes().isEmpty()) {
                loadPrefixes(configuration.getPrefixes(), writer);
            }
            //
            progressReport.start(inputRdf.size());
            for (ChunkedTriples.Chunk chunk : inputRdf) {
                for (Statement statement : chunk.toCollection()) {
                    writer.handleStatement(statement);
                }
                progressReport.entryProcessed();
            }
            progressReport.done();
            writer.endRDF();
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't write data.", ex);
        }
    }

    /**
     * Load prefixes from given turtle to the writer.
     *
     * @param turtle
     * @param writer
     */
    private void loadPrefixes(String turtle, RDFWriter writer)
            throws LpException {
        final RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        final InputStream stream;
        try {
            stream = new ByteArrayInputStream(
                    turtle.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure(
                    "Unsupported encoding exception.", ex);
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
