package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class TextHolder implements Component.Sequential {

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public TextHolderConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileName() == null
                || configuration.getFileName().isEmpty()) {
            throw exceptionFactory.missingConfigurationProperty(
                    TextHolderVocabulary.HAS_FILE_NAME);
        }
        //
        final File outputFile = outputFiles.createFile(
                configuration.getFileName()).toFile();
        try {
            Files.write(outputFile.toPath(),
                    configuration.getContent().getBytes(
                            Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw exceptionFactory.failed("Can't write content to file.", ex);
        }
    }

}
