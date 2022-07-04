package com.linkedpipes.plugin.extractor.pipelineInput;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.DefinitionReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

public class PipelineInput implements Component, SequentialExecution {

    private static final String HAS_INPUT_DIRECTORY =
            "http://linkedpipes.com/ontology/inputDirectory";

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public DefinitionReader definition;

    @Override
    public void execute() throws LpException {
        final Collection<String> values;
        try {
            values = definition.getProperties(HAS_INPUT_DIRECTORY);
        } catch (LpException ex) {
            throw new LpException("Can't read property.", ex);
        }
        //
        final File directory;
        if (values.size() != 1) {
            throw new LpException("Missing directory.");
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
            throw new LpException("Can't copy data.", ex);
        }
    }

}
