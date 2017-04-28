package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

public class ExcelToCsv implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public ExcelToCsvConfiguration configuration;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        parseFiles();
    }

    private void checkConfiguration() throws LpException {
        if (configuration.getFileNamePattern() == null
                || configuration.getFileNamePattern().isEmpty()) {
            throw exceptionFactory.failure(
                    ExcelToCsvVocabulary.HAS_FILE_NAME);
        }
    }

    private void parseFiles() throws LpException {
        WorkbookConverter parser = new WorkbookConverter(
                configuration, exceptionFactory, outputFiles);
        for (FilesDataUnit.Entry entry : inputFiles) {
            parser.processEntry(entry);
        }
    }

}
