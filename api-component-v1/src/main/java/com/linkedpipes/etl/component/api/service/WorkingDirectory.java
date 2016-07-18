package com.linkedpipes.etl.component.api.service;

import java.io.File;

/**
 * Provide access to working directory.
 *
 * @author Petr Škoda
 */
public class WorkingDirectory extends File {

    public WorkingDirectory(String pathname) {
        super(pathname);
        // Create the directory.
        super.mkdirs();
    }

}
