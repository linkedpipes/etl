package com.linkedpipes.plugin.transformer.packzip;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class PackZip implements Component.Sequential {

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public PackZipConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileName() == null
                || configuration.getFileName().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    PackZipVocabulary.HAS_FILE_NAME);
        }
        //
        final File zipFile = output.createFile(
                configuration.getFileName()).toFile();
        final byte[] buffer = new byte[8196];
        progressReport.start(input.size());
        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (FilesDataUnit.Entry entry : input) {
                addZipEntry(zos, buffer, entry);
                progressReport.entryProcessed();
            }
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't create archive.", ex);
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
    private void addZipEntry(ZipOutputStream zos, byte[] buffer,
            final FilesDataUnit.Entry entry) throws LpException {
        // Add to the zip file.
        final File sourceFile = entry.toFile();
        try (FileInputStream in = new FileInputStream(sourceFile)) {
            final ZipEntry ze = new ZipEntry(entry.getFileName());
            zos.putNextEntry(ze);
            // Copy data into zip file.
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } catch (Exception ex) {
            throw exceptionFactory.failure("", ex);
        }
    }

}
