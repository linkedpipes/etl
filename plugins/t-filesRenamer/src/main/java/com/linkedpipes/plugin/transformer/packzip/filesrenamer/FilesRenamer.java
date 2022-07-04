package com.linkedpipes.plugin.transformer.packzip.filesrenamer;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilesRenamer implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public FilesRenamerConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final Pattern pattern;
        try {
            pattern = Pattern.compile(configuration.getPattern());
        } catch (PatternSyntaxException ex) {
            throw new LpException("Invalid file pattern.", ex);
        }
        for (FilesDataUnit.Entry entry : inputFiles) {
            final String newName = pattern.matcher(entry.getFileName())
                    .replaceAll(configuration.getReplaceWith());
            // Copy file.
            final File targetFile = outputFiles.createFile(newName);
            targetFile.getParentFile().mkdirs();
            try {
                Files.copy(entry.toFile().toPath(), targetFile.toPath());
            } catch (IOException ex) {
                throw new LpException("Can't copy file.", ex);
            }
        }
    }

}
