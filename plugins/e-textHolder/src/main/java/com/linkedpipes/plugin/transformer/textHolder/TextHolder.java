package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public final class TextHolder implements Component, SequentialExecution {

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public TextHolderConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final File outputFile = outputFiles.createFile(
                configuration.getFileName()).toFile();
        final byte[] content;
        try {
            content = configuration.getContent().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("Can't resolved encoding.", ex);
        }
        try {
            Files.write(outputFile.toPath(), content);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't write content to file.", ex);
        }
    }
}
