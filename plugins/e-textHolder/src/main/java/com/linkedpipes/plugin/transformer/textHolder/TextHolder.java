package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class TextHolder implements SimpleExecution {

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public TextHolderConfiguration configuration;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        final File outputFile = outputFiles.createFile(configuration.getFileName()).toFile();
        try {
            Files.write(outputFile.toPath(), configuration.getContent().getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw new Component.ExecutionFailed("Can't write content to file.", ex);
        }
    }

}
