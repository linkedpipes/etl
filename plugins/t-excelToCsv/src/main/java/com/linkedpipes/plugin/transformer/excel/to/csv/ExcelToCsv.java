package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Petr Škoda
 */
public class ExcelToCsv implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelToCsv.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Configuration
    public ExcelToCsvConfiguration configuration;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        final Parser parser = new Parser(configuration);
        for (FilesDataUnit.Entry entry : inputFiles) {
            LOG.debug("Processing file:", entry.getFileName());
            parser.processEntry(entry, outputFiles, context);
        }
    }

}
