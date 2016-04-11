package com.linkedpipes.etl.dataunit.system.api.files;

import java.io.File;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;

/**
 *
 * @author Škoda Petr
 */
public interface WritableFilesDataUnit extends FilesDataUnit {

    /**
     *
     * @param fileName
     * @return Path to yet not existing file, in the data unit storage.
     * @throws SystemDataUnitException
     */
    public Entry createFile(String fileName) throws SystemDataUnitException;

    /**
     *
     * @return Root directory of the data unit storage.
     */
    public File getRootDirectory();

}
