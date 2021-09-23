package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;

import java.util.List;

public class FilesContainer {

    private final List<FilesDataUnit.Entry> files;

    public FilesContainer(List<FilesDataUnit.Entry> files) {
        this.files = files;
    }

    public List<FilesDataUnit.Entry> getFiles() {
        return files;
    }
    
}
