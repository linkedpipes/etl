package com.linkedpipes.etl.dataunit.system.files;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;

/**
 *
 * @author Škoda Petr
 */
class DirectoryIterator implements Iterator<FilesDataUnit.Entry> {

    private Iterator<File> fileIterator;

    private FilesDataUnitImpl.Entry nextEntry;

    private final Iterator<File> directoryIterator;

    private File currentDictionary;

    DirectoryIterator(Iterator<File> directoryIterator) {
        this.directoryIterator = directoryIterator;
        this.currentDictionary = directoryIterator.next();
        this.fileIterator = FileUtils.iterateFiles(currentDictionary, null, true);
        // Get next entry;
        this.nextEntry = getNext();
    }

    @Override
    public boolean hasNext() {
        return nextEntry != null;
    }

    @Override
    public FilesDataUnit.Entry next() {
        final FilesDataUnitImpl.Entry output = nextEntry;
        // Read next entry.
        nextEntry = getNext();
        if (output == null) {
            return null;
        } else {
            return output;
        }
    }

    private FilesDataUnitImpl.Entry getNext() {
        if (fileIterator.hasNext()) {
            final File nextFile = fileIterator.next();
            return new FilesDataUnitImpl.Entry(nextFile, currentDictionary);
        } else if (directoryIterator.hasNext()) {
            currentDictionary = directoryIterator.next();
            fileIterator = FileUtils.iterateFiles(currentDictionary, null, true);
            return getNext();
        } else {
            return null;
        }
    }

}
