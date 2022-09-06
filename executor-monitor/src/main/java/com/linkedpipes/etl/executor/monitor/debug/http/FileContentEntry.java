package com.linkedpipes.etl.executor.monitor.debug.http;

import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class FileContentEntry extends DebugEntry {

    final DataUnit dataUnit;

    final File file;

    final String source;

    final String publicPath;

    FileContentEntry(DataUnit dataUnit, File file, String source,
                     String publicPath) {
        this.dataUnit = dataUnit;
        this.file = file;
        this.source = source;
        this.publicPath = publicPath;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        ResponseContent content = new ResponseContent(Collections.emptyList());
        content.metadata.type = ResponseContent.TYPE_FILE;
        content.metadata.size = getFileSize();
        content.metadata.mimeType = getFileMimeType();
        content.metadata.publicDataPath = publicPath;
        contentAsJsonString = content.asJsonString();
        return this;
    }

    public String getFileMimeType() {
        return getMimeType(file);
    }

    public Long getFileSize() {
        return file.length();
    }

    public void writeFileContent(OutputStream outputStream) throws IOException {
        FileUtils.copyFile(file, outputStream);
    }

    public static String getMimeType(File file) {
        String fileName = file.getName().toLowerCase();
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex == -1) {
            return "text/plain; charset=utf-8";
        }
        String extension =  fileName.substring(extensionIndex);
        String mimeType;
        switch (extension) {
            case ".txt":
                mimeType = "text/plain";
                break;
            case ".html":
                mimeType = "text/html";
                break;
            case ".csv":
                mimeType = "text/csv";
                break;
            case ".json":
                mimeType = "application/json";
                break;
            case ".xml":
                mimeType = "application/xml";
                break;
            case ".jsonld":
                mimeType = "application/ld+json";
                break;
            case ".ttl":
                mimeType = "text/turtle";
                break;
            case ".trig":
                mimeType = "application/trig";
                break;
            case ".rdf":
                mimeType = "application/rdf+xml";
                break;
            case ".nt":
                mimeType = "application/n-triples";
                break;
            case ".nq":
                mimeType = "application/n-quads";
                break;
            default:
                mimeType = "text/plain";
                break;
        }
        return mimeType + "; charset=utf-8";
    }

}
