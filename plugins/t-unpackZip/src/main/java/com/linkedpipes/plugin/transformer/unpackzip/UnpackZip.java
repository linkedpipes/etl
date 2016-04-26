package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class UnpackZip implements SimpleExecution {

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public UnpackZipConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        progressReport.start(input.size());
        for (FilesDataUnit.Entry entry : input) {
            if (context.canceled()) {
                throw new Component.ExecutionCancelled();
            }
            // ..
            final File outputDirectory;
            if (configuration.isUsePrefix()) {
                outputDirectory = new File(output.getRootDirectory(), entry.getFileName());
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
    private void unzip(File zipFile, File targetDirectory) throws Component.ExecutionFailed {
        try {
            final ZipFile zip = new ZipFile(zipFile);
            if (zip.isEncrypted()) {
                throw new Component.ExecutionFailed("File is encrypted: {}", zipFile.getName());
            }
            zip.extractAll(targetDirectory.toString());
        } catch (ZipException ex) {
            throw new Component.ExecutionFailed("Extraction failed: {}", zipFile.getName(), ex);
        }
    }

}
