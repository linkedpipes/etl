package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

public final class TextHolder implements Component, SequentialExecution {

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public TextHolderConfiguration configuration;

    @Override
    public void execute() throws LpException {
        validateConfiguration();
        File outputFile = createOutputFile();
        byte[] content = getContentAsBytes();
        writeBytesToFile(outputFile, content);
    }

    private void validateConfiguration() throws LpException {
        String fileName = configuration.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            throw new LpException("Missing file name.", fileName);
        }
    }

    private File createOutputFile() throws LpException {
        return outputFiles.createFile(configuration.getFileName());
    }

    private byte[] getContentAsBytes() throws LpException {
        try {
            return configuration.getContent().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new LpException("Can't resolved encoding.", ex);
        }
    }

    private void writeBytesToFile(File file, byte[] content)
            throws LpException {
        try {
            Files.write(file.toPath(), content);
        } catch (IOException ex) {
            throw new LpException("Can't write content to file.", ex);
        }
    }

}
