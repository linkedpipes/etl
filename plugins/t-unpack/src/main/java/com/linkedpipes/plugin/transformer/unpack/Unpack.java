package com.linkedpipes.plugin.transformer.unpack;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class Unpack implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Unpack.class);

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public UnpackConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        LOG.info("Used extension option: {}", configuration.getFormat());
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
            unpack(entry, outputDirectory);
            //
            progressReport.entryProcessed();
        }
        progressReport.done();
        //
    }

    private void unpack(FilesDataUnit.Entry inputEntry, File targetDirectory) throws NonRecoverableException {
        final String extension = getExtension(inputEntry);
        try (final InputStream stream = new FileInputStream(inputEntry.toFile())) {
            switch (extension) {
                case "zip":
                    unpackZip(stream, targetDirectory);
                    break;
                case "bz2":
                    unpackBzip2(stream, targetDirectory, inputEntry);
                    break;
                default:
                    throw new Component.ExecutionFailed("Unknown file format (" + extension + ") : " +
                            inputEntry.getFileName());
            }
        } catch (IOException | ArchiveException ex) {
            throw new Component.ExecutionFailed("Extraction failed: {}", inputEntry.getFileName(), ex);
        }
    }

    /**
     * Return lower case extension of file, or extension defined by configuration.
     *
     * @param entry
     * @return
     */
    private String getExtension(FilesDataUnit.Entry entry) {
        if (configuration.getFormat() == null || configuration.getFormat().isEmpty()) {
            LOG.debug("No format setting provided, autodetection used as default.");
            configuration.setFormat(UnpackVocabulary.FORMAT_DETECT);
        }
        switch (configuration.getFormat()) {
            case UnpackVocabulary.FORMAT_ZIP:
                return ArchiveStreamFactory.ZIP;
            case UnpackVocabulary.FORMAT_BZIP2:
                return "bz2";
            case UnpackVocabulary.FORMAT_DETECT:
            default:
                final String fileName = entry.getFileName();
                return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
    }

    /**
     * Unpack ZIP archive.
     *
     * @param inputStream
     * @param targetDirectory
     * @throws IOException
     * @throws ArchiveException
     */
    private static void unpackZip(InputStream inputStream, File targetDirectory) throws IOException, ArchiveException {
        try (ArchiveInputStream archiveStream = new ArchiveStreamFactory().createArchiveInputStream("zip", inputStream)) {
            ZipArchiveEntry entry;
            while ((entry = (ZipArchiveEntry) archiveStream.getNextEntry()) != null) {
                final File entryFile = new File(targetDirectory, entry.getName());
                // Create directories based on file path.
                if (entry.getName().endsWith("/")) {
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                    continue;
                }
                if (entryFile.isDirectory() || entryFile.exists()) {
                    continue;
                }
                // Copy stream to file.
                try (FileOutputStream out = new FileOutputStream(entryFile)) {
                    final byte[] buffer = new byte[8196];
                    int length;
                    while ((length = archiveStream.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                        out.flush();
                    }
                }
            }
        }
    }

    private static void unpackBzip2(InputStream inputStream, File targetDirectory, FilesDataUnit.Entry inputEntry) throws IOException {
        try (final BZip2CompressorInputStream bzip2Stream = new BZip2CompressorInputStream(inputStream, true)) {
            final String outputFileName = inputEntry.getFileName().substring(0, inputEntry.getFileName().lastIndexOf("."));
            final File outputFile = new File(targetDirectory, outputFileName);
            outputFile.getParentFile().mkdirs();
            // Copy stream to file.
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                final byte[] buffer = new byte[8196];
                int length;
                while ((length = bzip2Stream.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                    out.flush();
                }
            }
        }
    }

}
