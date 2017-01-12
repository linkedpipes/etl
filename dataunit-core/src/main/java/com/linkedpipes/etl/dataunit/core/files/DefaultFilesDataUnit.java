package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.JsonUtils;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO Do not require working directory for input data unit.
 */
class DefaultFilesDataUnit
        implements FilesDataUnit, WritableFilesDataUnit, ManageableDataUnit {

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

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultFilesDataUnit.class);

    private final String binding;

    private final String iri;

    private final File writeDirectory;

    private final List<File> readDirectories = new LinkedList<>();

    /**
     * List of data unit sources.
     */
    private final Collection<String> sources;

    public DefaultFilesDataUnit(String binding, String iri,
            File writeDirectory, Collection<String> sources) {
        this.binding = binding;
        this.iri = iri;
        this.writeDirectory = writeDirectory;
        this.sources = sources;
        //
        if (this.writeDirectory != null) {
            writeDirectory.mkdirs();
            this.readDirectories.add(writeDirectory);
        }
    }

    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public Entry createFile(String fileName) throws LpException {
        final File output = new File(writeDirectory, fileName);
        if (output.exists()) {
            throw new LpException("File already exists.");
        }
        output.getParentFile().mkdirs();
        return new Entry(output, writeDirectory);
    }

    @Override
    public File getWriteDirectory() {
        return writeDirectory;
    }

    @Override
    public void initialize(File directory) throws LpException {
        final File file = new File(directory, "data.json");
        JsonUtils.loadCollection(file, String.class).stream().forEach(
                (entry) -> readDirectories.add(new File(directory, entry))
        );
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        //
        if (writeDirectory == null) {
            throw new LpException("WriteDirectory is not set for: {}", iri);
        }
        // Iterate over sources and add their content.
        for (String iri : sources) {
            if (!dataUnits.containsKey(iri)) {
                throw new LpException("Missing input: {}", iri);
            }
            final ManageableDataUnit dataunit = dataUnits.get(iri);
            if (dataunit instanceof DefaultFilesDataUnit) {
                merge((DefaultFilesDataUnit) dataunit);
            } else {
                throw new LpException(
                        "Can't merge with source data unit: {} of type {}",
                        iri, dataunit.getClass().getSimpleName());
            }
        }
    }

    @Override
    public List<File> save(File directory) throws LpException {
        final Path dirPath = directory.toPath();
        final List<String> directories = readDirectories.stream().map(
                (file) -> dirPath.relativize(file.toPath()).toString()
        ).collect(Collectors.toList());
        JsonUtils.save(new File(directory, "data.json"), directories);
        return readDirectories;
    }

    @Override
    public void close() throws LpException {
        // No operation here.
    }

    @Override
    public Collection<File> getReadDirectories() {
        return Collections.unmodifiableCollection(readDirectories);
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
        final Iterator<File> directoryIterator = readDirectories.iterator();
        if (!directoryIterator.hasNext()) {
            return Collections.EMPTY_LIST.iterator();
        }
        return new DirectoryIterator(directoryIterator);
    }

    private void merge(DefaultFilesDataUnit source) {
        readDirectories.addAll(source.readDirectories);
    }

}
