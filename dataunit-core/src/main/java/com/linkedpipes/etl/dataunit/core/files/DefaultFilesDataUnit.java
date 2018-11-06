package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.AbstractDataUnit;
import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO Do not require working directory for input data unit.
 */
class DefaultFilesDataUnit
        extends AbstractDataUnit
        implements FilesDataUnit, WritableFilesDataUnit {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultFilesDataUnit.class);

    private final File writeDirectory;

    private final List<File> dataDirectories = new LinkedList<>();

    public DefaultFilesDataUnit(
            DataUnitConfiguration configuration,
            Collection<String> sources) {
        super(configuration, sources);
        //
        this.writeDirectory = configuration.getWorkingDirectory();
        if (this.writeDirectory != null) {
            this.writeDirectory.mkdirs();
            this.dataDirectories.add(this.writeDirectory);
        }
    }


    @Override
    public File createFile(String fileName) throws LpException {
        File output = new File(this.writeDirectory, fileName);
        if (output.exists()) {
            throw new LpException(
                    "File already exists: {} ({})", fileName, output);
        }
        output.getParentFile().mkdirs();
        return output;
    }

    @Override
    public File getWriteDirectory() {
        return this.writeDirectory;
    }

    @Override
    public void initialize(File directory) throws LpException {
        dataDirectories.clear();
        dataDirectories.addAll(loadDataDirectories(directory));
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        initializeFromSource(dataUnits);
    }

    @Override
    public void save(File directory) throws LpException {
        saveDataDirectories(directory, dataDirectories);
        saveDebugDirectories(directory, dataDirectories);
    }

    @Override
    public void close() {
        // No operation here.
    }

    @Override
    public Collection<File> getReadDirectories() {
        return Collections.unmodifiableCollection(dataDirectories);
    }

    @Override
    public long size() {
        Date start = new Date();
        long size = 0;
        for (FilesDataUnit.Entry item : this) {
            ++size;
        }
        LOG.debug("Computing size takes: {} ms",
                (new Date()).getTime() - start.getTime());
        return size;
    }

    @Override
    public Iterator<Entry> iterator() {
        Iterator<File> directoryIterator = dataDirectories.iterator();
        if (!directoryIterator.hasNext()) {
            return Collections.EMPTY_LIST.iterator();
        }
        return new DirectoryIterator(directoryIterator);
    }

    @Override
    protected List<File> loadDataDirectories(File directory)
            throws LpException {
        File dataFile = new File(directory, "data.json");
        boolean currentVersion = dataFile.exists();
        if (currentVersion) {
            return loadRelativePaths(directory, "data.json");
        } else {
            return loadDataDirectoriesBackwardCompatible(directory);
        }
    }

    protected List<File> loadDataDirectoriesBackwardCompatible(File directory)
            throws LpException {
        return loadRelativePaths(new File(directory, "data"), "data.json");
    }

    @Override
    protected void merge(ManageableDataUnit dataUnit) throws LpException {
        if (dataUnit instanceof DefaultFilesDataUnit) {
            DefaultFilesDataUnit source = (DefaultFilesDataUnit) dataUnit;
            dataDirectories.addAll(source.dataDirectories);
        } else {
            throw new LpException(
                    "Can't merge with source data unit: {} of type {}",
                    getIri(), dataUnit.getClass().getSimpleName());
        }
    }

}
