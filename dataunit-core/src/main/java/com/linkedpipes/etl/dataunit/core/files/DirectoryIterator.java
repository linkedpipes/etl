package com.linkedpipes.etl.dataunit.core.files;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

/**
 * Implementation of iterator that recursively iterate over
 * directories.
 */
class DirectoryIterator implements Iterator<FilesDataUnit.Entry> {

    private Iterator<File> fileIterator;

    /**
     * Entry returned by next call of {@link #next()}.
     */
    private FilesDataUnit.Entry nextEntry;

    /**
     * Iterate over directories.
     */
    private final Iterator<File> directoryIterator;

    private File currentDirectory;

    public DirectoryIterator(Iterator<File> directoryIterator) {
        this.directoryIterator = directoryIterator;
        this.currentDirectory = directoryIterator.next();
        this.fileIterator = FileUtils.iterateFiles(
                currentDirectory, null, true);
        this.nextEntry = getNext();
    }

    @Override
    public boolean hasNext() {
        return nextEntry != null;
    }

    @Override
    public FilesDataUnit.Entry next() {
        FilesDataUnit.Entry output = this.nextEntry;
        // Read next entry.
        this.nextEntry = getNext();
        if (output == null) {
            return null;
        } else {
            return output;
        }
    }

    /**
     * @return Next file entry or null.
     */
    private FilesDataUnit.Entry getNext() {
        if (this.fileIterator.hasNext()) {
            File nextFile = this.fileIterator.next();
            return new DefaultEntry(nextFile, this.currentDirectory);
        } else if (this.directoryIterator.hasNext()) {
            this.currentDirectory = this.directoryIterator.next();
            this.fileIterator = FileUtils.iterateFiles(
                    this.currentDirectory, null, true);
            return getNext();
        } else {
            return null;
        }
    }

}
