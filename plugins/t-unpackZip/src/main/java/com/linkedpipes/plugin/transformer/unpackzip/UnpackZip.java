package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 *
 * @author Škoda Petr
 */
public final class UnpackZip implements SequentialExecution {

    @DataProcessingUnit.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @DataProcessingUnit.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @DataProcessingUnit.Configuration
    public UnpackZipConfiguration configuration;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        progressReport.startTotalUnknown(10);
        for (FilesDataUnit.Entry entry : input) {
            if (context.canceled()) {
                throw new DataProcessingUnit.ExecutionCancelled();
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
            unzip(entry.getPath(), outputDirectory);
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
    private void unzip(File zipFile, File targetDirectory) throws DataProcessingUnit.ExecutionFailed {
        try {
            final ZipFile zip = new ZipFile(zipFile);
            if (zip.isEncrypted()) {
                throw new DataProcessingUnit.ExecutionFailed("File is encrypted: " + zipFile.getName());
            }
            zip.extractAll(targetDirectory.toString());
        } catch (ZipException ex) {
            throw new DataProcessingUnit.ExecutionFailed(ex, "Extraction failed: " + zipFile.getName());
        }
    }

}
