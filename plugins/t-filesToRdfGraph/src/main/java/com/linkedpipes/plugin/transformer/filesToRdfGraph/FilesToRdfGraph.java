package com.linkedpipes.plugin.transformer.filesToRdfGraph;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Škoda Petr
 */
public final class FilesToRdfGraph implements Component.Sequential {

    private static final Logger LOG =
            LoggerFactory.getLogger(FilesToRdfGraph.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(id = "OutputRdf")
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
