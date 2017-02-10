package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class FilesToRdf implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(FilesToRdf.class);

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        // Prepare parsers and inverters.
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
            // Create output graph.
            final IRI outputGraph = outputRdf.createGraph();
            rdfInserter.setTargetGraph(outputGraph);
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
            LOG.debug("Loading: {} -> {} : {}", file.getFileName(), outputGraph,
                    format);
            final RDFParser rdfParser = Rio.createParser(format);
            rdfParser.setRDFHandler(rdfInserter);
            try (final InputStream fileStream = new FileInputStream(
                    file.toFile())) {
                rdfParser.parse(fileStream, "http://localhost/base/");
            } catch (IOException | RDFHandlerException | RDFParseException ex) {
                throw exceptionFactory
                        .failure("Can't parse file: {}", file.getFileName(),
                                ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
