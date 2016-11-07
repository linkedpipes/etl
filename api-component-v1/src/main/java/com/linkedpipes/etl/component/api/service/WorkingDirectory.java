package com.linkedpipes.etl.component.api.service;

import java.io.File;

/**
 * Provide access to an existing working directory in form of an {@link File}.
 */
public class WorkingDirectory extends File {

    public WorkingDirectory(String pathname) {
        super(pathname);
        super.mkdirs();
    }

}
