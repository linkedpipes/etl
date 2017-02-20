package com.linkedpipes.plugin.transformer.filesToRdfGraph;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class FilesToRdfGraph implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(FilesToRdfGraph.class);

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public FilesToRdfGraphConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final StatementInserter rdfInserter = new StatementInserter(
                configuration.getCommitSize(), outputRdf);
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
        // Load files.
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry file : inputFiles) {
            //
            final RDFFormat format;
            if (defaultFormat == null) {
                final Optional<RDFFormat> optionalFormat
                        = Rio.getParserFormatForFileName(file.getFileName());
                if (!optionalFormat.isPresent()) {
                    throw exceptionFactory.failure(
                            "Can't determine format for file: {}",
                            file.getFileName());
                }
                format = optionalFormat.get();
            } else {
                format = defaultFormat;
            }
            LOG.debug("Loading: {}", file.getFileName());
            final RDFParser rdfParser = Rio.createParser(format);
            rdfParser.setRDFHandler(rdfInserter);
            try (final InputStream fileStream = new FileInputStream(
                    file.toFile())) {
                rdfParser.parse(fileStream, "http://localhost/base/");
            } catch (IOException | RDFHandlerException | RDFParseException ex) {
                if (configuration.isSkipOnFailure()) {
                    LOG.error("Can't parse file: {}", file.getFileName(), ex);
                } else {
                    throw exceptionFactory.failure(
                            "Can't parse file: {}", file.getFileName(), ex);
                }
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
