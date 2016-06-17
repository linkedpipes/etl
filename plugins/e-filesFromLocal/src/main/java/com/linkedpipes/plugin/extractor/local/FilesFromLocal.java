package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Petr Škoda
 */
public class FilesFromLocal implements Component.Sequential {

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public FilesFromLocalConfiguration configuration;

    @Override
    public void execute() throws NonRecoverableException {
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
            throw exceptionFactory.failed("Can't copy file.", ex);
        }
    }

}
