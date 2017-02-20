package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelToCsv implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelToCsv.class);

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
        if (configuration.getFileNamePattern() == null
                || configuration.getFileNamePattern().isEmpty()) {
            throw exceptionFactory.failure(
                    ExcelToCsvVocabulary.HAS_FILE_NAME);
        }
        //
        final Parser parser = new Parser(configuration);
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing file:", entry.getFileName());
            parser.processEntry(entry, outputFiles, exceptionFactory);
        }
    }

}
