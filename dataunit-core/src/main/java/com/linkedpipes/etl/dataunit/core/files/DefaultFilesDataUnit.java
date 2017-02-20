package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.BaseDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * TODO Do not require working directory for input data unit.
 */
class DefaultFilesDataUnit
        extends BaseDataUnit
        implements FilesDataUnit, WritableFilesDataUnit, ManageableDataUnit {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultFilesDataUnit.class);

    private final File writeDirectory;

    private final List<File> dataDirectories = new LinkedList<>();

    public DefaultFilesDataUnit(String binding, String iri,
            File writeDirectory, Collection<String> sources) {
        super(binding, iri, sources);
        this.writeDirectory = writeDirectory;
        //
        if (this.writeDirectory != null) {
            writeDirectory.mkdirs();
            this.dataDirectories.add(writeDirectory);
        }
    }

    @Override
    public File createFile(String fileName) throws LpException {
        final File output = new File(writeDirectory, fileName);
        if (output.exists()) {
            throw new LpException("File already exists: {} ({})",
                    fileName, output);
        }
        output.getParentFile().mkdirs();
        return output;
    }

    @Override
    public File getWriteDirectory() {
        return writeDirectory;
    }

    @Override
    public void initialize(File directory) throws LpException {
        dataDirectories.clear();
        dataDirectories.addAll(loadDataDirectories(directory));
    }

    @Override
    public void save(File directory) throws LpException {
        saveDataDirectories(directory, dataDirectories);
        saveDebugDirectories(directory, dataDirectories);
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    public Collection<File> getReadDirectories() {
        return Collections.unmodifiableCollection(dataDirectories);
    }

    @Override
    public long size() {
        LOG.debug("Computing size ...");
        long size = 0;
        for (FilesDataUnit.Entry item : this) {
            ++size;
        }
        LOG.debug("Computing size ... done");
        return size;
    }

    @Override
    public Iterator<FilesDataUnit.Entry> iterator() {
        final Iterator<File> directoryIterator = dataDirectories.iterator();
        if (!directoryIterator.hasNext()) {
            return Collections.EMPTY_LIST.iterator();
        }
        return new DirectoryIterator(directoryIterator);
    }

    @Override
    protected List<File> loadDataDirectories(File directory)
            throws LpException {
        final File dataFile = new File(directory, "data.json");
        final boolean currentVersion = dataFile.exists();
        if (currentVersion) {
            return loadRelativePaths(directory, "data.json");
        } else {
            return loadRelativePaths(new File(directory, "data"), "data.json");
        }
    }

    @Override
    protected void merge(ManageableDataUnit dataunit) throws LpException {
        if (dataunit instanceof DefaultFilesDataUnit) {
            final DefaultFilesDataUnit source =
                    (DefaultFilesDataUnit) dataunit;
            dataDirectories.addAll(source.dataDirectories);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataunit.getClass().getSimpleName());
        }
    }

}
