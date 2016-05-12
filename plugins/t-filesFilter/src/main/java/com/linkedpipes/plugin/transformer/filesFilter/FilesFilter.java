package com.linkedpipes.plugin.transformer.filesFilter;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Petr Škoda
 */
public class FilesFilter implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(FilesFilter.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Configuration
    public FilesFilterConfiguration configuration;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        final String pattern = configuration.getFileNamePattern();
        LOG.debug("Pattern: {}", pattern);
        for (FilesDataUnit.Entry entry : inputFiles) {
            final boolean matches = entry.getFileName().matches(pattern);
            LOG.debug("Entry: {} : {}", entry.getFileName(), matches);
            if (matches) {
                final File outputFile = outputFiles.createFile(entry.getFileName()).toFile();
                try {
                    Files.copy(entry.toFile().toPath(), outputFile.toPath());
                } catch (IOException ex) {
                    throw new Component.ExecutionFailed("Can't copy file: {}", entry.getFileName(), ex);
                }
            }
        }
    }

}
