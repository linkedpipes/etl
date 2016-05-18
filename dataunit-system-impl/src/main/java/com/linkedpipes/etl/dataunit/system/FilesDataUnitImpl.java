package com.linkedpipes.etl.dataunit.system;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import java.io.IOException;

/**
 *
 * @author Škoda Petr
 */
public final class FilesDataUnitImpl implements ManagableFilesDataUnit {

    /**
     * Implementation of files data unit entry.
     */
    static class Entry implements FilesDataUnit.Entry {

        private final File file;

        private final File root;

        Entry(File file, File root) {
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

    private final String id;

    private final String resourceUri;

    private boolean initialized = false;

    /**
     * Write directory, is also part of {@link #readRootDirectories}.
     */
    private final File rootDirectory;

    /**
     * Directories with content of this data unit.
     */
    private List<File> readRootDirectories = new LinkedList<>();

    /**
     * List of source data units IRI.
     */
    private final Collection<String> sources;

    public FilesDataUnitImpl(FilesDataUnitConfiguration configuration) {
        this.id = configuration.getBinding();
        this.resourceUri = configuration.getResourceUri();
        this.rootDirectory = configuration.getWorkingDirectory();
        this.sources = configuration.getSourceDataUnitUris();
        // Add root to 'read' directories.
        if (rootDirectory != null) {
            this.readRootDirectories.add(rootDirectory);
        }
    }

    private void merge(FilesDataUnitImpl source) throws DataUnitException {
        readRootDirectories.addAll(source.readRootDirectories);
    }

    @Override
    public void initialize(File directory) throws DataUnitException {
        final ObjectMapper mapper = new ObjectMapper();
        final File inputFile = new File(directory, "data.json");
        final JavaType type = mapper.getTypeFactory().constructCollectionType(
                List.class, File.class);
        try {
            readRootDirectories = mapper.readValue(inputFile, type);
        } catch (IOException ex) {
            throw new DataUnitException("Can't load directory list.", ex);
        }
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits)
            throws DataUnitException {
        initialized = true;
        // Iterate over sources and add their content.
        for (String sourceUri : sources) {
            if (!dataUnits.containsKey(sourceUri)) {
                throw new DataUnitException("Missing input!");
            }
            final ManagableDataUnit dataunit = dataUnits.get(sourceUri);
            if (dataunit instanceof FilesDataUnitImpl) {
                merge((FilesDataUnitImpl) dataunit);
            } else {
                throw new DataUnitException(
                        "Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void save(File directory) throws DataUnitException {
        final ObjectMapper mapper = new ObjectMapper();
        final File outputFile = new File(directory, "data.json");
        // Load read directories.
        try {
            mapper.writeValue(outputFile, readRootDirectories);
        } catch (IOException ex) {
            throw new DataUnitException("Can't save directory list.", ex);
        }
    }

    @Override
    public List<File> dumpContent(File directory) throws DataUnitException {
        return readRootDirectories;
    }

    @Override
    public void close() throws DataUnitException {
        // No operation here.
    }

    @Override
    public String getBinding() {
        return id;
    }

    @Override
    public String getResourceIri() {
        return resourceUri;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public Entry createFile(String fileName) throws SystemDataUnitException {
        if (rootDirectory == null) {
            throw new SystemDataUnitException("Root directory is not set!");
        }
        final File output = new File(rootDirectory, fileName);
        output.getParentFile().mkdirs();
        return new Entry(output, rootDirectory);
    }

    @Override
    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public Iterator<FilesDataUnit.Entry> iterator() {
        final Iterator<File> directoryIterator = readRootDirectories.iterator();
        if (!directoryIterator.hasNext()) {
            return Collections.EMPTY_LIST.iterator();
        }
        return new DirectoryIterator(directoryIterator);
    }

    @Override
    public Collection<File> getReadRootDirectories() {
        return Collections.unmodifiableCollection(readRootDirectories);
    }

    @Override
    public long size() {
        // TODO We should use better approeach here.
        long size = 0;
        for (com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit.Entry item : this) {
            ++size;
        }
        return size;
    }

}
