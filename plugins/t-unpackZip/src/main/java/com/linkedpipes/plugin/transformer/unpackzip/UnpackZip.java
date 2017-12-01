package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;

public final class UnpackZip implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public UnpackZipConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        progressReport.start(input.size());
        for (FilesDataUnit.Entry entry : input) {
            final File outputDirectory;
            if (configuration.isUsePrefix()) {
                outputDirectory = new File(output.getWriteDirectory(),
                        entry.getFileName());
            } else {
                outputDirectory = output.getWriteDirectory();
            }
            outputDirectory.mkdirs();
            // Unpack.
            unzip(entry.toFile(), outputDirectory);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    /**
     * Extract given zip file into given directory.
     *
     * @param zipFile
     * @param targetDirectory
     */
    private void unzip(File zipFile, File targetDirectory) throws LpException {
        try {
            final ZipFile zip = new ZipFile(zipFile);
            if (zip.isEncrypted()) {
                throw exceptionFactory.failure("File is encrypted: {}",
                        zipFile.getName());
            }
            zip.extractAll(targetDirectory.toString());
        } catch (ZipException ex) {
            throw exceptionFactory.failure("Extraction failure: {}",
                    zipFile.getName(), ex);
        }
    }

}
