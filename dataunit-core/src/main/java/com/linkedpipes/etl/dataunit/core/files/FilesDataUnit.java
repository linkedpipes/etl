package com.linkedpipes.etl.dataunit.core.files;

import java.io.File;
import java.util.Collection;

/**
 * DataUnit can contains same file multiple times - if such file is presented
 * in multiple sources used to create this data unit.
 *
 * When iterated only presented files are returned, the iteration does not
 * return directories.
 */
public interface FilesDataUnit extends Iterable<FilesDataUnit.Entry> {

    interface Entry {

        /**
         * @return System path to file.
         */
        File toFile();

        /**
         * @return File name of the file, ie. relative path from the root
         * of data unit.
         */
        String getFileName();

    }

    /**
     * Content of returned directories should not be modified!
     *
     * @return Collection of root directories with content of the data unit.
     */
    Collection<File> getReadDirectories();

    /**
     * @return Undefined value.
     */
    long size();

}
