package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Chunked version of RdfToFile.
 */
public final class RdfToFileChunked implements Component.Sequential {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfToFileChunked.class);

    private static final String FILE_ENCODE = "UTF-8";

    @Component.InputPort(id = "InputRdf")
    public ChunkedStatements inputRdf;

    @Component.OutputPort(id = "OutputFile")
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
                configuration.getFileName()).toFile();
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
            //
            writer.startRDF();
            progressReport.start(inputRdf.size());
            for (ChunkedStatements.Chunk chunk : inputRdf) {
                for (Statement statement : chunk.toStatements()) {
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

}
