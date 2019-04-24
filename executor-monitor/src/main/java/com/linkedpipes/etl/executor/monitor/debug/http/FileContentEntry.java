package com.linkedpipes.etl.executor.monitor.debug.http;

import com.linkedpipes.etl.executor.monitor.debug.DataUnit;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Collections;

public class FileContentEntry extends DebugEntry {

    final DataUnit dataUnit;

    final File file;

    final String source;

    FileContentEntry(DataUnit dataUnit, File file, String source) {
        this.dataUnit = dataUnit;
        this.file = file;
        this.source = source;
    }

    @Override
    public DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException {
        ResponseContent content = new ResponseContent(Collections.emptyList());
        content.metadata.type = ResponseContent.TYPE_FILE;
        content.metadata.size = getFileSize();
        content.metadata.mimeType = getFileMimeType();
        contentAsString = content.asJsonString();
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
        return URLConnection.getFileNameMap().getContentTypeFor(file.getName());
    }

}
