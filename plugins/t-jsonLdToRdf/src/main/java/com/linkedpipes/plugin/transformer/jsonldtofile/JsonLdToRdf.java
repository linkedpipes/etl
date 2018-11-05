package com.linkedpipes.plugin.transformer.jsonldtofile;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.semarglproject.jsonld.JsonLdParser;
import org.semarglproject.rdf4j.core.sink.RDF4JSink;
import org.semarglproject.source.StreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonLdToRdf implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(JsonLdToRdf.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public JsonLdToRdfConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private BufferedWriter writer;

    @Override
    public void execute() throws LpException {
        prepareWriter();
        loadFiles();
    }

    private void prepareWriter() {
        writer = new BufferedWriter(configuration.getCommitSize(), outputRdf);
    }

    private void loadFiles() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            writer.setTargetGraph(outputRdf.getWriteGraph());
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
        StreamProcessor streamProcessor = new StreamProcessor(
                JsonLdParser.connect(FileAwareRdf4jSink.connect(writer)));
        try  {
            streamProcessor.process(entry.toFile());
        } catch (Exception ex) {
            handleLoadingException(entry.getFileName(), ex);
        }
    }

    private void handleLoadingException(String fileName, Exception ex)
            throws LpException {
        throw exceptionFactory.failure(
                "Can't parse file: {}", fileName, ex);
    }

}
