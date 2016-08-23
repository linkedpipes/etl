package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Petr Škoda
 */
public class ExcelToCsv implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelToCsv.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputFiles")
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
