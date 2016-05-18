package com.linkedpipes.etl.dataunit.system.api.files;

import java.io.File;
import java.util.Collection;

/**
 * DataUnit can contains same file multiple times - if such file is presented
 * in multiple sources used to create this data unit.
 *
 * When iterated only presented files are returned, the iteration does not
 * return directories.
 *
 * @author Škoda Petr
 */
public interface FilesDataUnit extends Iterable<FilesDataUnit.Entry> {

    public interface Entry {

        /**
         *
         * @return System path to file.
         */
        public File toFile();

        /**
         *
         * @return File name of the file, ie. relative path from the root
         * of data unit.
         */
        public String getFileName();

    }

    /**
     * Content of returned directories should not be modified!
     *
     * @return Collection of root directories with content of the data unit.
     */
    public Collection<File> getReadRootDirectories();

    /**
     *
     * @return Undefined value.
     */
    public long size();

}
