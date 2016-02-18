package com.linkedpipes.plugin.transformer.packzip;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Škoda Petr
 */
public final class PackZip implements SequentialExecution {

    @DataProcessingUnit.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @DataProcessingUnit.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @DataProcessingUnit.Configuration
    public PackZipConfiguration configuration;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        final File zipFile = output.createFile(configuration.getFileName());
        final byte[] buffer = new byte[8196];
        try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (FilesDataUnit.Entry entry : input) {
                if (context.canceled()) {
                    throw new DataProcessingUnit.ExecutionCancelled();
                }
                // ...
                addZipEntry(zos, buffer, entry);
                progressReport.entryProcessed();
            }
        } catch (IOException ex) {
            throw new DataProcessingUnit.ExecutionFailed("Can't create archive.", ex);
        }
        progressReport.done();
    }

    /**
     * Add single file into stream as zip entry.
     *
     * @param zos
     * @param buffer
     * @param entry
     * @throws DataUnitException
     */
    private void addZipEntry(ZipOutputStream zos, byte[] buffer, final FilesDataUnit.Entry entry)
            throws DataProcessingUnit.ExecutionFailed {
        // Add to the zip file.
        final File sourceFile = entry.getPath();
        try (FileInputStream in = new FileInputStream(sourceFile)) {
            final ZipEntry ze = new ZipEntry(entry.getFileName());
            zos.putNextEntry(ze);
            // Copy data into zip file.
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } catch (Exception ex) {
            throw new DataProcessingUnit.ExecutionFailed("", ex);
        }
    }

}
