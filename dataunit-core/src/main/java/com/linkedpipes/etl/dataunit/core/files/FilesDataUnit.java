package com.linkedpipes.etl.dataunit.core.files;

import java.io.File;
import java.util.Collection;

/**
 * DataUnit can contains same file multiple times - if such file is presented
 * in multiple sources used to create this data unit.
 *
 * <p>When iterated only presented files are returned, the iteration does not
 * return directories.
 */
public interface FilesDataUnit extends Iterable<FilesDataUnit.Entry> {

    interface Entry {

        /**
         * Return system path to file.
         */
        File toFile();

        /**
         * Return file name of the file, ie. relative path from the root
         * of data unit.
         */
        String getFileName();

    }

    /**
     * Content of returned directories should not be modified.
     *
     * @return Collection of root directories with content of the data unit.
     */
    Collection<File> getReadDirectories();

    /**
     * Number of files in FilesDataUnit.
     */
    long size();

}
