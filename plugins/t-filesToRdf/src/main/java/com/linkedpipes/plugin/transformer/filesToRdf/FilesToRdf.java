package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.openrdf.model.IRI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class FilesToRdf implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(FilesToRdf.class);

    @Component.OutputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        // Prepare parsers and inserters.
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
            LOG.debug("Loading: {} -> {} : {}", file.getFileName(), outputGraph, format);
            final RDFParser rdfParser = Rio.createParser(format);
            rdfParser.setRDFHandler(rdfInserter);
            try (final InputStream fileStream = new FileInputStream(file.toFile())) {
                rdfParser.parse(fileStream, "http://localhost/base/");
            } catch (IOException | RDFHandlerException | RDFParseException ex) {
                throw exceptionFactory.failure("Can't parse file: {}", file.getFileName(), ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
