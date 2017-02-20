package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FilesFromLocal implements Component, SequentialExecution {

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public FilesFromLocalConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final File source = new File(configuration.getPath());
        if (!source.exists()) {
            throw exceptionFactory.failure(
                    "Source directory does not exists: {}",
                    configuration.getPath()
            );
        }
        //
        if (source.isDirectory()) {
            // Copy all files in a directory.
            final Path rootPath = source.toPath();
            final File[] files = source.listFiles();
            if (files == null) {
                throw exceptionFactory.failure("Method listFiles return null. "
                        + "Please check privileges.");
            }
            for (File file : files) {
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
     */
    private void copy(File file, String fileName) throws LpException {
        final File destination = output.createFile(fileName);
        try {
            if (file.isDirectory()) {
                FileUtils.copyDirectory(file, destination);
            } else {
                FileUtils.copyFile(file, destination);
            }
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't copy file.", ex);
        }
    }

}
