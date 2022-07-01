package com.linkedpipes.etl.executor.api.v1.service;

import java.io.File;
import java.net.URI;

/**
 * Provide access to an existing working directory in form of an {@link File}.
 */
public class WorkingDirectory extends File {

    public WorkingDirectory(File file) {
        super(file.toURI());
        super.mkdirs();
    }

    public WorkingDirectory(URI uri) {
        super(uri);
        super.mkdirs();
    }

}
