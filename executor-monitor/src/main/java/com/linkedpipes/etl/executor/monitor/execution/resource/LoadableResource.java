package com.linkedpipes.etl.executor.monitor.execution.resource;

import com.linkedpipes.etl.executor.monitor.execution.Execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface LoadableResource {

    /**
     * Called if the resource should be updated from the file
     * and the file is missing.
     *
     * @param execution
     */
    void missing(Execution execution);

    void load(InputStream stream) throws IOException;

    void writeToStream(OutputStream stream) throws IOException;

    String getRelativeUrlPath();

    String getRelativeFilePath();

}
