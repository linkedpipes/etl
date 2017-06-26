package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
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

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

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

    private RDFFormat defaultFormat;

    private List<Statement> buffer = new ArrayList<>(100000);

    @Override
    public void execute() throws LpException {
        prepareDefaultFormat();
        loadFiles();
    }

    private void prepareDefaultFormat() {
        String mimeType = configuration.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            defaultFormat = null;
        } else {
            Optional<RDFFormat> format = Rio.getParserFormatForMIMEType(
                    configuration.getMimeType());
            if (format.isPresent()) {
                defaultFormat = format.get();
            } else {
                defaultFormat = null;
            }
        }
    }

    private void loadFiles() throws LpException {
        progressReport.start(inputFiles.size());
        int filesCounter = 0;
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Loading: {}", entry.getFileName());
            loadEntry(entry);
            ++filesCounter;
            if (filesCounter >= configuration.getFilesPerChunk()) {
                flushBuffer();
                filesCounter = 0;
            }
            progressReport.entryProcessed();
        }
        flushBuffer();
        progressReport.done();
    }

    private void flushBuffer() throws LpException {
        if (buffer.isEmpty()) {
            return;
        }
        outputRdf.submit(buffer);
        buffer.clear();
    }

    private void loadEntry(FilesDataUnit.Entry entry) throws LpException {
        RDFFormat format = getFormat(entry.getFileName());
        loadFile(entry.toFile(), format);
    }

    private RDFFormat getFormat(String fileName) throws LpException {
        if (defaultFormat != null) {
            return defaultFormat;
        }
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (!format.isPresent()) {
            throw exceptionFactory.failure(
                    "Can't determine format for file: {}", fileName);
        }
        return format.get();
    }

    private void loadFile(File file, RDFFormat format) throws LpException {
        try (InputStream stream = new FileInputStream(file)) {
            final RDFParser parser = createParser(format);
            parser.parse(stream, "http://localhost/base/");
        } catch (IOException ex) {
            exceptionFactory.failure("Can't load file: {}", file, ex);
        }
    }

    private final RDFParser createParser(RDFFormat format) {
        RDFHandler handler = new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st) {
                buffer.add(st);
            }
        };
        if (format == RDFFormat.JSONLD) {
            handler = new BlankNodePrefixUpdater(handler);
        }
        RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(handler);
        return parser;
    }

}
