package com.linkedpipes.plugin.transformer.packzip;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class PackZip implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public PackZipConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileName() == null
                || configuration.getFileName().isEmpty()) {
            throw new LpException("Missing property: {}",
                    PackZipVocabulary.HAS_FILE_NAME);
        }
        //
        final File zipFile = output.createFile(
                configuration.getFileName());
        final byte[] buffer = new byte[8196];
        progressReport.start(input.size());
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (FilesDataUnit.Entry entry : input) {
                addZipEntry(zos, buffer, entry);
                progressReport.entryProcessed();
            }
        } catch (IOException ex) {
            throw new LpException("Can't create archive.", ex);
        }
        progressReport.done();
    }

    /**
     * Add single file into stream as zip entry.
     *
     * @param zos
     * @param buffer
     * @param entry
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
            throw new LpException("", ex);
        }
    }

}
