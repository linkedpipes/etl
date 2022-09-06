package com.linkedpipes.etl.executor.monitor.debug.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class DebugEntry {

    @FunctionalInterface
    public interface CreatePublicPath {

        String apply(File file);

    }

    protected String contentAsJsonString = null;

    public abstract DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException;

    public int getSize() {
        return contentAsJsonString.length();
    }

    public void write(OutputStream outputStream) throws IOException {
        byte[] content = contentAsJsonString.getBytes(StandardCharsets.UTF_8);
        outputStream.write(content);
    }

}
