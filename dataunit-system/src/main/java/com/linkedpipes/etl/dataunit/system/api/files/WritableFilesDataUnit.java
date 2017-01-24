package com.linkedpipes.etl.dataunit.system.api.files;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

import java.io.File;

public interface WritableFilesDataUnit extends FilesDataUnit {

    /**
     * @param fileName
     * @return Path to yet not existing file, in the data unit storage.
     */
    public Entry createFile(String fileName) throws LpException;

    /**
     * @return Root directory of the data unit storage.
     */
    public File getRootDirectory();

    /**
     * Add external directory as a reference.
     *
     * @param directory
     */
    public void addDirectory(File directory);

}
