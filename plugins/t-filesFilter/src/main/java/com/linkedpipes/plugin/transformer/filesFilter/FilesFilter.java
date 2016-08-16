package com.linkedpipes.plugin.transformer.filesFilter;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Petr Škoda
 */
public class FilesFilter implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(FilesFilter.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public FilesFilterConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileNamePattern() == null
                || configuration.getFileNamePattern().isEmpty()) {
            throw exceptionFactory.missingRdfProperty(
                    FilesFilterVocabulary.HAS_PATTERN);
        }
        //
        final String pattern = configuration.getFileNamePattern();
        LOG.debug("Pattern: {}", pattern);
        for (FilesDataUnit.Entry entry : inputFiles) {
            final boolean matches = entry.getFileName().matches(pattern);
            LOG.debug("Entry: {} : {}", entry.getFileName(), matches);
            if (matches) {
                final File outputFile = outputFiles.createFile(
                        entry.getFileName()).toFile();
                try {
                    Files.copy(entry.toFile().toPath(), outputFile.toPath());
                } catch (IOException ex) {
                    throw exceptionFactory.failure("Can't copy file: {}",
                            entry.getFileName(), ex);
                }
            }
        }
    }

}
