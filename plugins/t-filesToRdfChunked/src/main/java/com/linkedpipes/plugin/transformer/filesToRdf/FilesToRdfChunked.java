package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FilesToRdfChunked implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(FilesToRdfChunked.class);

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    /**
     * Buffer used to store data.
     */
    private List<Statement> buffer = new ArrayList<>(100000);

    @Override
    public void execute() throws LpException {
        final RDFFormat defaultFormat;
        if (configuration.getMimeType() == null
                || configuration.getMimeType().isEmpty()) {
            defaultFormat = null;
        } else {
            final Optional<RDFFormat> optionalFormat
                    = Rio.getParserFormatForMIMEType(
                    configuration.getMimeType());
            if (optionalFormat.isPresent()) {
                defaultFormat = optionalFormat.get();
            } else {
                defaultFormat = null;
            }
        }
        //
        int counter = 0;
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Loading: {}", entry.getFileName());
            if (defaultFormat == null) {
                final RDFFormat format = Rio.getParserFormatForFileName(
                        entry.getFileName()).orElseGet(null);
                if (format == null) {
                    throw exceptionFactory.failure(
                            "Can't determine format for file: {}",
                            entry.getFileName());
                }
                loadFile(entry.toFile(), format);
            } else {
                loadFile(entry.toFile(), defaultFormat);
            }
            counter++;
            if (counter >= configuration.getFilesPerChunk()) {
                outputRdf.submit(buffer);
                buffer.clear();
                counter = 0;
            }
            progressReport.entryProcessed();
        }
        if (!buffer.isEmpty()) {
            outputRdf.submit(buffer);
        }
        progressReport.done();
    }

    private void loadFile(File file, RDFFormat format) throws LpException {
        try (InputStream stream = new FileInputStream(file)) {
            final RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    buffer.add(st);
                }
            });
            parser.parse(stream, "http://localhost/base/");
        } catch (IOException ex) {
            exceptionFactory.failure("Can't load file: {}", file, ex);
        }
    }

}
