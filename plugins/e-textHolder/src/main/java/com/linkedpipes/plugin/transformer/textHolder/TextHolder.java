package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 *
 * @author Škoda Petr
 */
public final class TextHolder implements SequentialExecution {

    @DataProcessingUnit.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @DataProcessingUnit.Configuration
    public TextHolderConfiguration configuration;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        final File outputFile = outputFiles.createFile(configuration.getFileName());
        try {
            Files.write(outputFile.toPath(), configuration.getContent().getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw new DataProcessingUnit.ExecutionFailed("Can't write content to file.", ex);
        }
    }

}
