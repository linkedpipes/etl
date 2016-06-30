package com.linkedpipes.plugin.transformer.packzip.filesrenamer;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Petr Škoda
 */
public class FilesRenamer implements Component.Sequential {

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public FilesRenamerConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final Pattern pattern;
        try {
            pattern = Pattern.compile(configuration.getPattern());
        } catch (PatternSyntaxException ex) {
            throw exceptionFactory.invalidConfigurationProperty(
                    "",
                    "", ex);
        }
        for (FilesDataUnit.Entry entry : inputFiles) {
            final String newName = pattern.matcher(entry.getFileName())
                    .replaceAll(configuration.getReplaceWith());
            // Copy file.
            final File targetFile = outputFiles.createFile(newName).toFile();
            targetFile.getParentFile().mkdirs();
            try {
                Files.copy(entry.toFile().toPath(), targetFile.toPath());
            } catch (IOException ex) {
                throw exceptionFactory.failed("Can't copy file.", ex);
            }
        }
    }

}
