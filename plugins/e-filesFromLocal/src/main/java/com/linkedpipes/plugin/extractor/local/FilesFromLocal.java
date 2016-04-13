package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.Component;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class FilesFromLocal implements SimpleExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(FilesFromLocal.class);

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public FilesFromLocalConfiguration configuration;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        final File sourceRoot = new File(configuration.getPath());
        for (File file : sourceRoot.listFiles()) {
            final String fileName =
                    sourceRoot.toPath().relativize(file.toPath()).toString();
            final File destination = output.createFile(fileName).toFile();
            // Copy file.
            try {
                Files.copy(file.toPath(), destination.toPath());
            } catch (IOException ex) {
                LOG.error("{} -> {}", file, destination);
                throw new ExecutionFailed("Can't copy file.", ex);
            }
        }
    }

}
