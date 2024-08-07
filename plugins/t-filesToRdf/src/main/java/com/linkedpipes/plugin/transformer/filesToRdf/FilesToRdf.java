package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class FilesToRdf implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(FilesToRdf.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private RDFFormat defaultFormat;

    private BufferedWriter inserter;

    @Override
    public void execute() throws LpException {
        prepareDefaultFormat();
        prepareStatementInserter();
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

    private void prepareStatementInserter() {
        inserter = new BufferedWriter(configuration.getCommitSize(), outputRdf);
    }

    private void loadFiles() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            inserter.setTargetGraph(outputRdf.createGraph());
            try {
                loadEntry(entry);
            } catch (LpException ex) {
                if (configuration.isSkipOnFailure()) {
                    LOG.error("Can't load file: {}", entry.getFileName());
                } else {
                    throw  ex;
                }
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void loadEntry(FilesDataUnit.Entry entry) throws LpException {
        RDFFormat format = getFormat(entry.getFileName());
        RDFParser parser = createParser(format);

        if (parser instanceof JSONLDParser jsonLdParser) {
            jsonLdParser.set(JSONLDSettings.SECURE_MODE, false);
        }

        try (InputStream fileStream = new FileInputStream(entry.toFile())) {
            parser.parse(fileStream, "http://localhost/base/");
        } catch (IOException | RDFHandlerException | RDFParseException ex) {
            handleLoadingException(entry.getFileName(), ex);
        }
    }

    private RDFFormat getFormat(String fileName) throws LpException {
        if (defaultFormat != null) {
            return defaultFormat;
        }
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (!format.isPresent()) {
            throw new LpException(
                    "Can't determine format for file: {}", fileName);
        }
        return format.get();
    }

    private RDFParser createParser(RDFFormat format) {
        RDFHandler handler = inserter;
        if (format == RDFFormat.JSONLD) {
            handler = new BlankNodePrefixUpdater(handler);
        }

        RDFParser rdfParser = Rio.createParser(format);
        rdfParser.setRDFHandler(handler);
        return rdfParser;
    }

    private void handleLoadingException(String fileName, Exception ex)
            throws LpException {
        throw new LpException(
                "Can't parse file: {}", fileName, ex);
    }

}
