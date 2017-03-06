package com.linkedpipes.plugin.transformer.jsontojsonld;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonToJsonLd implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public JsonToJsonLdConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry inputEntry : inputFiles) {
            File outputFile = outputFiles.createFile(inputEntry.getFileName());
            updateFile(inputEntry, outputFile);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void updateFile(FilesDataUnit.Entry inputEntry, File outputFile)
            throws LpException {
        File inputFile = inputEntry.toFile();
        try (InputStream inputStream = new AddContextStream(
                configuration.getVocabulary(),
                configuration.getEncoding(),
                new FileInputStream(inputFile),
                inputEntry.getFileName())) {
            FileUtils.copyInputStreamToFile(inputStream, outputFile);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't update file: {} -> {}",
                    inputFile, outputFile, ex);
        }
    }

}
