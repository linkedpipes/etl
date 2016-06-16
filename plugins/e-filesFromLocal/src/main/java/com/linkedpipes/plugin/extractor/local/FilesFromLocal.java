package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.executable.SimpleExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
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
        final File source = new File(configuration.getPath());
        if (source.isDirectory()) {
            // Copy all files in a directory.
            final Path rootPath = source.toPath();
            for (File file : source.listFiles()) {
                final Path relativePath = rootPath.relativize(file.toPath());
                copy(file, relativePath.toString());
            }
        } else {
            copy(source, source.getName());
        }
    }

    /**
     * Add given file/directory to output under given name.
     *
     * @param file Path to file to add.
     * @param fileName Name of added file.
     * @throws SystemDataUnitException
     * @throws com.linkedpipes.etl.dpu.api.Component.ExecutionFailed
     */
    private void copy(File file, String fileName)
            throws SystemDataUnitException, ExecutionFailed {
        final File destination = output.createFile(fileName).toFile();
        try {
            if (file.isDirectory()) {
                FileUtils.copyDirectory(file, destination);
            } else {
                FileUtils.copyFile(file, destination);
            }
        } catch (IOException ex) {
            LOG.error("{} -> {}", file, destination);
            throw new ExecutionFailed("Can't copy file.", ex);
        }
    }

}
