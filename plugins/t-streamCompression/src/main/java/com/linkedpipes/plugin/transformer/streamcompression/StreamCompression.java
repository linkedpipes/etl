package com.linkedpipes.plugin.transformer.streamcompression;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class StreamCompression implements Component.Sequential {

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public StreamCompressionConfiguration configuration;

    private final byte[] buffer = new byte[32 * 1024];

    @Override
    public void execute() throws LpException {
        progressReport.start(input.size());
        for (FilesDataUnit.Entry entry : input) {
            final File inFile = entry.toFile();
            try {
                switch (configuration.getFormat()) {
                    case StreamCompressionVocabulary.FORMAT_BZ2:
                        bzip2(inFile, output.createFile(
                                entry.getFileName() + ".bz2").toFile());
                        break;
                    case StreamCompressionVocabulary.FORMAT_GZIP:
                        gzip(inFile, output.createFile(
                                entry.getFileName() + ".gz").toFile());
                        break;
                    default:
                        break;
                }
            } catch (IOException ex) {
                exceptionFactory.failure(
                        "Can't compress file: {}", entry.getFileName(), ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void bzip2(File inFile, File outFile) throws IOException {
        BZip2CompressorInputStream a;

        try (final BZip2CompressorOutputStream outStream =
                     new BZip2CompressorOutputStream(
                             new FileOutputStream(outFile));
             final FileInputStream inStream = new FileInputStream(inFile)) {
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, len);
            }
            outStream.finish();
        }
    }

    private void gzip(File inFile, File outFile) throws IOException {
        try (final GZIPOutputStream outStream =
                     new GZIPOutputStream(new FileOutputStream(outFile));
             final FileInputStream inStream = new FileInputStream(inFile)) {
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, len);
            }
            outStream.finish();
        }
    }

}

