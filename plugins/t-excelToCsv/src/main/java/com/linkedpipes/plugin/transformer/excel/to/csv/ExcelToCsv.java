package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;

public class ExcelToCsv implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

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
            throw new LpException(
                    ExcelToCsvVocabulary.HAS_FILE_NAME);
        }
    }

    private void parseFiles() throws LpException {
        WorkbookConverter parser = new WorkbookConverter(
                configuration, outputFiles);
        for (FilesDataUnit.Entry entry : inputFiles) {
            parser.processEntry(entry);
        }
    }

}
