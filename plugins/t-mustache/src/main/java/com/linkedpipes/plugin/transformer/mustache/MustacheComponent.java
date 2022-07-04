package com.linkedpipes.plugin.transformer.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.*;
import java.util.List;

public final class MustacheComponent implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit input;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public MustacheConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private Integer fileNameCounter = 0;

    @Override
    public void execute() throws LpException {
        List<ObjectDataHolder> data = loadData();
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

    private List<ObjectDataHolder> loadData() throws LpException {
        DataObjectLoader dataObjectLoader = new DataObjectLoader(configuration);
        return dataObjectLoader.loadData(input);
    }

    private Mustache createMustache() {
        String template = MustacheTemplatePrefixExpander.expand(
                configuration.getTemplate());
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(template), "template");
    }

    private void outputData(Mustache mustache, List<ObjectDataHolder> data)
            throws LpException {
        progressReport.start(data.size());
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
                throw new LpException("Can't write output file.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
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
