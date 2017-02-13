package com.linkedpipes.plugin.extractor.pipelineInput;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.DefinitionReader;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

public class PipelineInput implements Component, SequentialExecution {

    private static final String HAS_INPUT_DIRECTORY
            = "http://linkedpipes.com/resources/components/e-pipelineInput/"
            + "inputDirectory";

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public DefinitionReader definition;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final Collection<String> values;
        try {
            values = definition.getProperties(HAS_INPUT_DIRECTORY);
        } catch (LpException ex) {
            throw exceptionFactory.failure("Can't read property.", ex);
        }
        //
        final File directory;
        if (values.size() != 1) {
            throw exceptionFactory.failure("Missing directory.");
        }
        directory = new File(URI.create(values.iterator().next()));
        if (!directory.isDirectory()) {
            // Empty input.
            return;
        }
        try {
            //
            FileUtils.copyDirectory(directory, output.getWriteDirectory());
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't copy data.", ex);
        }
    }

}
