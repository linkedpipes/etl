package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
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
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class FilesToRdf implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(FilesToRdf.class);

    @Component.OutputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        // Prepare parsers and inserters.
        final StatementInserter rdfInserter = new StatementInserter(configuration.getCommitSize(), context, outputRdf);
        final RDFFormat defaultFormat;
        if (configuration.getMimeType() == null || configuration.getMimeType().isEmpty()) {
            defaultFormat = null;
        } else {
            final Optional<RDFFormat> optionalFormat = Rio.getParserFormatForMIMEType(configuration.getMimeType());
            if (optionalFormat.isPresent()) {
                defaultFormat = optionalFormat.get();
            } else {
                defaultFormat = null;
            }
        }
        // Load files.
        for (FilesDataUnit.Entry file : inputFiles) {
            // Create output graph.
            final IRI outputGraph = outputRdf.createGraph();
            rdfInserter.setTargetGraph(outputGraph);
            //
            final RDFFormat format;
            if (defaultFormat == null) {
                final Optional<RDFFormat> optionalFormat = Rio.getParserFormatForFileName(file.getFileName());
                if (!optionalFormat.isPresent()) {
                    throw new Component.ExecutionFailed("Can't determine format for file:" + file.getFileName());
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
                throw new Component.ExecutionFailed("Can't parse file: {}", file.getFileName(), ex);
            }
            if (context.canceled()) {
                throw new Component.ExecutionCancelled();
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
