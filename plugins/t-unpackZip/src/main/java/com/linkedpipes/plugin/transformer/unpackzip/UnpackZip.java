package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.File;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class UnpackZip implements Component.Sequential {

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
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
                outputDirectory = new File(output.getRootDirectory(),
                        entry.getFileName());
            } else {
                outputDirectory = output.getRootDirectory();
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
     * @throws DPUException
     */
    private void unzip(File zipFile, File targetDirectory) throws LpException {
        try {
            final ZipFile zip = new ZipFile(zipFile);
            if (zip.isEncrypted()) {
                throw exceptionFactory.failed("File is encrypted: {}",
                        zipFile.getName());
            }
            zip.extractAll(targetDirectory.toString());
        } catch (ZipException ex) {
            throw exceptionFactory.failed("Extraction failed: {}",
                    zipFile.getName(), ex);
        }
    }

}
