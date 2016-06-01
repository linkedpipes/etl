package com.linkedpipes.plugin.extractor.httpget;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.Component;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.service.DefinitionReader;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class PipelineInput implements SimpleExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineInput.class);

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public DefinitionReader definition;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        final Collection<String> values;
        try {
            values = definition.getProperty(
                    "http://linkedpipes.com/resources/components/e-pipelineInput/inputDirectory");
        } catch (DefinitionReader.OperationFailed ex) {
            throw new ExecutionFailed("Can't rea property.", ex);
        }
        //
        final File directory;
        if (values.size() != 1) {
            throw new ExecutionFailed("Missing directory.");
        }
        directory = new File(URI.create(values.iterator().next()));
        if (!directory.isDirectory()) {
            // Empty input.
            return;
        }
        try {
            //
            FileUtils.copyDirectory(directory, output.getRootDirectory());
        } catch (IOException ex) {
            throw new ExecutionFailed("Can't copy data.", ex);
        }
    }

}
