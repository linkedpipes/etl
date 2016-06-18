package com.linkedpipes.etl.dataunit.system.api.files;

import java.io.File;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public interface WritableFilesDataUnit extends FilesDataUnit {

    /**
     *
     * @param fileName
     * @return Path to yet not existing file, in the data unit storage.
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public Entry createFile(String fileName) throws LpException;

    /**
     *
     * @return Root directory of the data unit storage.
     */
    public File getRootDirectory();

}
