package com.linkedpipes.etl.plugin.transformer.filedecode;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class FileDecode implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(FileDecode.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public FileDecodeConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            final File outputFile = outputFiles.createFile(
                    entry.getFileName()).toFile();
            try (InputStream input = new FileInputStream(entry.toFile())) {
                FileUtils.copyInputStreamToFile(
                        Base64.getDecoder().wrap(input),
                        outputFile);
            } catch (IOException ex) {
                if (configuration.isSkipOnError()) {
                    LOG.warn("Invalid file ignored", ex);
                } else {
                    throw exceptionFactory.failure("Can't decode file: {}",
                            entry, ex);
                }
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
