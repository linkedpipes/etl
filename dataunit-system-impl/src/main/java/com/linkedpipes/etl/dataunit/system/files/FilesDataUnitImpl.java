package com.linkedpipes.etl.dataunit.system.files;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Škoda Petr
 */
final class FilesDataUnitImpl implements ManagableFilesDataUnit {

    static class Entry implements FilesDataUnit.Entry {

        private final File file;

        private final File root;

        Entry(File file, File root) {
            this.file = file;
            this.root = root;
        }

        @Override
        public File getPath() {
            return file;
        }

        @Override
        public String getFileName() {
            return root.toPath().relativize(file.toPath()).toString();
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(FilesDataUnitImpl.class);

    private final String id;

    private final String resourceUri;

    private boolean initialized = false;

    private final File rootDirectory;

    private final List<File> readRootDirectories = new LinkedList<>();

    private final Collection<String> sources;

    private final File debugDirectory;

    public FilesDataUnitImpl(FilesDataUnitConfiguration configuration) {
        this.id = configuration.getBinding();
        this.resourceUri = configuration.getResourceUri();
        this.rootDirectory = configuration.getWorkingDirectory();
        this.sources = configuration.getSourceDataUnitUris();
        this.debugDirectory = configuration.getDebugDirectory();
        // Add root to 'read' directories.
        if (rootDirectory != null) {
            this.readRootDirectories.add(rootDirectory);
        }
    }

    private void merge(FilesDataUnitImpl source) throws DataUnitException {
        readRootDirectories.add(source.rootDirectory);
    }

    @Override
    public void initialize(Map<String, ManagableDataUnit> dataUnits) throws DataUnitException {
        LOG.info("initialize");
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
                throw new DataUnitException("Can't merge with source data unit!");
            }
        }
        initialized = true;
    }

    @Override
    public void dumpContent() throws DataUnitException {
        if (this.debugDirectory == null) {
            return;
        }
        // Create a file with reference to all sources.
        final File infoFile = new File(this.debugDirectory, "info.dat");
        boolean first = true;
        try (FileWriter fileWriter = new FileWriter(infoFile)) {
            for (File directory : readRootDirectories) {
                if (first) {
                    first = false;
                } else {
                    fileWriter.append("\n");
                }
                fileWriter.append(directory.getPath());
            }
        } catch (IOException ex) {
            throw new DataUnitException("Can't write debug data.", ex);
        }
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
    public String getResourceUri() {
        return resourceUri;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public File createFile(String fileName) throws SystemDataUnitException {
        if (rootDirectory == null) {
            throw new SystemDataUnitException("Root directory is not set!");
        }
        final File output = new File(rootDirectory, fileName);
        output.getParentFile().mkdirs();
        return output;
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

}
