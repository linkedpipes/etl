package com.linkedpipes.plugin.transformer.unpack;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;

public final class Unpack implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Unpack.class);

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public UnpackConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        LOG.info("Used format option: {}", configuration.getFormat());
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
            unpack(entry, outputDirectory);
            //
            progressReport.entryProcessed();
        }
        progressReport.done();
        //
    }

    private void unpack(FilesDataUnit.Entry inputEntry, File targetDirectory)
            throws LpException {
        final String extension = getExtension(inputEntry);
        if (ArchiveStreamFactory.SEVEN_Z.equals(extension)) {
            try {
                unpackSevenZip(inputEntry.toFile(), targetDirectory);
                return;
            } catch (IOException ex) {
                throw exceptionFactory.failure("Extraction failure: {}",
                        inputEntry.getFileName(), ex);
            }
        }
        try (final InputStream stream = new FileInputStream(
                inputEntry.toFile())) {
            switch (extension) {
                case ArchiveStreamFactory.ZIP:
                    unpackZip(stream, targetDirectory);
                    break;
                case "bz2":
                    unpackBzip2(stream, targetDirectory, inputEntry);
                    break;
                case "gz":
                    unpackGzip(stream, targetDirectory, inputEntry);
                    break;
                default:
                    throw exceptionFactory.failure("Unknown file format (" +
                            extension + ") : " + inputEntry.getFileName());
            }
        } catch (IOException | ArchiveException ex) {
            throw exceptionFactory.failure("Extraction failure: {}",
                    inputEntry.getFileName(), ex);
        }
    }

    /**
     * Return lower case extension of file, or extension defined by
     * configuration.
     *
     * @param entry
     * @return
     */
    private String getExtension(FilesDataUnit.Entry entry) {
        if (configuration.getFormat() == null ||
                configuration.getFormat().isEmpty()) {
            LOG.debug("No format setting provided.");
            configuration.setFormat(UnpackVocabulary.FORMAT_DETECT);
        }
        switch (configuration.getFormat()) {
            case UnpackVocabulary.FORMAT_ZIP:
                return ArchiveStreamFactory.ZIP;
            case UnpackVocabulary.FORMAT_7ZIP:
                return ArchiveStreamFactory.SEVEN_Z;
            case UnpackVocabulary.FORMAT_BZIP2:
                return "bz2";
            case UnpackVocabulary.FORMAT_GZIP:
                return "gz";
            case UnpackVocabulary.FORMAT_DETECT:
            default:
                final String fileName = entry.getFileName();
                return fileName.substring(fileName.lastIndexOf(".") + 1)
                        .toLowerCase();
        }
    }

    /**
     * Unpack ZIP archive.
     *
     * @param inputStream
     * @param directory
     */
    private static void unpackZip(InputStream inputStream,
            File directory) throws IOException, ArchiveException {
        try (ArchiveInputStream archive = new ArchiveStreamFactory()
                .createArchiveInputStream("zip", inputStream)) {
            ZipArchiveEntry entry;
            while ((entry = (ZipArchiveEntry) archive.getNextEntry()) != null) {
                final File entryFile = new File(directory, entry.getName());
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
                copyToFile(archive, entryFile);
            }
        }
    }

    /**
     * Unpack 7Zip archive.
     *
     * @param inputFile
     * @param directory
     */
    private static void unpackSevenZip(File inputFile,
            File directory) throws IOException {
        final SevenZFile sevenZFile = new SevenZFile(inputFile);
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        while (entry != null) {
            final File outputFile = new File(directory, entry.getName());
            outputFile.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                // Read.
                final int bufferSize = 64000;
                byte[] buffer = new byte[bufferSize];
                while (true) {
                    int read = sevenZFile.read(buffer, 0, bufferSize);
                    if (read == -1) {
                        break;
                    }
                    out.write(buffer, 0, read);
                }
            }
            entry = sevenZFile.getNextEntry();
        }
        sevenZFile.close();
    }

    /**
     * Unpack Bzip2 archive.
     *
     * @param inputStream
     * @param targetDirectory
     * @param inputEntry
     */
    private static void unpackBzip2(InputStream inputStream,
            File targetDirectory, FilesDataUnit.Entry inputEntry)
            throws IOException {
        try (final BZip2CompressorInputStream bzip2Stream
                     = new BZip2CompressorInputStream(inputStream, true)) {
            final String outputFileName = inputEntry.getFileName()
                    .substring(0, inputEntry.getFileName().lastIndexOf("."));
            final File outputFile = new File(targetDirectory, outputFileName);
            outputFile.getParentFile().mkdirs();
            // Copy stream to file.
            copyToFile(bzip2Stream, outputFile);
        }
    }

    /**
     * Unpack GZip archive.
     *
     * @param inputStream
     * @param targetDirectory
     * @param inputEntry
     */
    private static void unpackGzip(InputStream inputStream,
            File targetDirectory, FilesDataUnit.Entry inputEntry)
            throws IOException {
        String outputFileName = inputEntry.getFileName();
        if (outputFileName.toLowerCase().endsWith(".gz")) {
            outputFileName = outputFileName.substring(0,
                    outputFileName.length() - 3);
        }
        try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {
            copyToFile(gzipStream, new File(targetDirectory, outputFileName));
        }
    }

    /**
     * Write given stream to a file.
     *
     * @param stream
     * @param file
     */
    private static void copyToFile(InputStream stream, File file)
            throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            final byte[] buffer = new byte[8196];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                out.flush();
            }
        }
    }

}
