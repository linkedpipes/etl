package com.linkedpipes.plugin.transformer.mustachechunked;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.*;
import java.util.List;

public final class MustacheChunkedComponent
        implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples input;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public MustacheConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private Integer fileNameCounter = 0;

    private DataObjectLoader dataObjectLoader;

    @Override
    public void execute() throws LpException {
        dataObjectLoader = new DataObjectLoader(configuration);
        progressReport.start(input.size());
        for (ChunkedTriples.Chunk chunk : input) {
            processChunk(chunk);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void processChunk(ChunkedTriples.Chunk chunk) throws LpException {
        List<ObjectDataHolder> data = dataObjectLoader.loadData(
                chunk.toCollection());
        // If there is no input add an empty object.
        // https://github.com/linkedpipes/etl/issues/152
        if (data.isEmpty()) {
            ObjectDataHolder emptyOutput = new ObjectDataHolder();
            emptyOutput.output = true;
            data.add(emptyOutput);
        }
        //
        Mustache mustache = createMustache();
        outputData(mustache, data);
    }

    private Mustache createMustache() {
        String template = MustacheTemplatePrefixExpander.expand(
                configuration.getTemplate());
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(template), "template");
    }

    private void outputData(Mustache mustache, List<ObjectDataHolder> data)
            throws LpException {
        for (ObjectDataHolder object : data) {
            if (object.data == null) {
                continue;
            }
            String fileName = getFileName(object);
            File outputFile = output.createFile(fileName);
            try (OutputStreamWriter outputStream = new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF8")) {
                mustache.execute(outputStream, object.data).flush();
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't write output file.", ex);
            }
        }
    }

    private String getFileName(ObjectDataHolder object) {
        if (object.fileName != null) {
            return object.fileName;
        } else {
            fileNameCounter += 1;
            return "output_" + fileNameCounter;
        }
    }

}
