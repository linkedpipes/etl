package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FilesToRdfChunked implements Component.Sequential {

    private static final Logger LOG =
            LoggerFactory.getLogger(FilesToRdfChunked.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(id = "OutputRdf")
    public WritableChunkedStatements outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    /**
     * Buffer used to store data.
     */
    private List<Statement> buffer = new ArrayList<>(10000);

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
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void loadFile(File file, RDFFormat format) throws LpException {
        buffer.clear();
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
        outputRdf.submit(buffer);
    }

}
