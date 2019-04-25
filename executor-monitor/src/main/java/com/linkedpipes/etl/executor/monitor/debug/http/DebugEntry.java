package com.linkedpipes.etl.executor.monitor.debug.http;

import java.io.IOException;
import java.io.OutputStream;

public abstract class DebugEntry {

    protected String contentAsString = null;

    public abstract DebugEntry prepareData(
            String nameFilter, String sourceFilter, long offset, long limit)
            throws IOException;

    public int getSize() {
        return contentAsString.length();
    }

    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(contentAsString.getBytes("utf-8"));
    }

}
