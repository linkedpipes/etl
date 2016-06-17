package com.linkedpipes.plugin.extractor.pipelineInput;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.DefinitionReader;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Petr Škoda
 */
public class PipelineInput implements Component.Sequential {

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public DefinitionReader definition;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws NonRecoverableException {
        final Collection<String> values;
        try {
            values = definition.getProperty(
                    "http://linkedpipes.com/resources/components/e-pipelineInput/inputDirectory");
        } catch (DefinitionReader.OperationFailed ex) {
            throw exceptionFactory.failed("Can't read property.", ex);
        }
        //
        final File directory;
        if (values.size() != 1) {
            throw exceptionFactory.failed("Missing directory.");
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
            throw exceptionFactory.failed("Can't copy data.", ex);
        }
    }

}
