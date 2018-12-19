package com.linkedpipes.plugin.transformer.jsonldtofile;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.semarglproject.jsonld.JsonLdParser;
import org.semarglproject.source.StreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonLdToRdfChunked implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(JsonLdToRdfChunked.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public JsonLdToRdfChunkedConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private StatementsCollector collector = new StatementsCollector();

    @Override
    public void execute() throws LpException {
        progressReport.start(inputFiles.size());
        int filesCounter = 0;
        for (FilesDataUnit.Entry entry : inputFiles) {
            try {
                loadEntry(entry);
            } catch (LpException ex) {
                if (configuration.isSkipOnFailure()) {
                    LOG.error("Can't load file: {}", entry.getFileName());
                } else {
                    throw  ex;
                }
            }
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

    private void loadEntry(FilesDataUnit.Entry entry) throws LpException {
        StreamProcessor streamProcessor = new StreamProcessor(
                JsonLdParser.connect(FileAwareRdf4jSink.connect(collector)));
        try  {
            streamProcessor.process(entry.toFile());
        } catch (Exception ex) {
            handleLoadingException(entry.getFileName(), ex);
        }
        if (configuration.isFileReference()) {
            ValueFactory valueFactory = SimpleValueFactory.getInstance();
            collector.add(valueFactory.createStatement(
                    valueFactory.createBNode(),
                    valueFactory.createIRI(configuration.getFilePredicate()),
                    valueFactory.createLiteral(entry.getFileName())
            ));
        }
    }

    private void handleLoadingException(String fileName, Exception ex)
            throws LpException {
        throw exceptionFactory.failure(
                "Can't parse file: {}", fileName, ex);
    }

    private void flushBuffer() throws LpException {
        if (collector.getStatements().isEmpty()) {
            return;
        }
        outputRdf.submit(collector.getStatements());
        collector.clear();
    }


}
