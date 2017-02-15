package com.linkedpipes.etl.dataunit.core.files;

import java.io.File;

class DefaultEntry implements FilesDataUnit.Entry {

    private final File file;

    private final File root;

    public DefaultEntry(File file, File root) {
        this.file = file;
        this.root = root;
    }

    @Override
    public File toFile() {
        return file;
    }

    @Override
    public String getFileName() {
        return root.toPath().relativize(file.toPath()).toString();
    }

}
