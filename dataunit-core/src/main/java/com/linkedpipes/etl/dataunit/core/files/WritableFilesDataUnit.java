package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;

public interface WritableFilesDataUnit {

    /**
     * Fail if the file of given name already exists.
     *
     * @return Path to yet not existing file, in the data unit storage.
     */
    File createFile(String fileName) throws LpException;

    /**
     * Return write root directory.
     */
    File getWriteDirectory();

}
