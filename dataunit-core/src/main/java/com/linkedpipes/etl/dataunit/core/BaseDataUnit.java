package com.linkedpipes.etl.dataunit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provide functionality for management of data and debug directories.
 */
public abstract class BaseDataUnit implements ManageableDataUnit {

    private final String binding;

    private final String iri;

    private final Collection<String> sources;

    public BaseDataUnit(String binding, String iri,
            Collection<String> sources) {
        this.binding = binding;
        this.iri = iri;
        this.sources = sources;
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        // Iterate over sources and add their content.
        for (String iri : sources) {
            if (!dataUnits.containsKey(iri)) {
                throw new LpException("Missing input: {}", iri);
            }
            merge(dataUnits.get(iri));
        }
    }

    /**
     * Merge content of given data unit to this data unit.
     *
     * @param dataUnit
     */
    protected abstract void merge(ManageableDataUnit dataUnit)
            throws LpException;

    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public void referenceContent(File source, File destination)
            throws LpException {
        saveDataDirectories(destination, loadDataDirectories(source));
        saveDebugDirectories(destination, loadDebugDirectories(source));
    }

    protected List<File> loadDataDirectories(File directory)
            throws LpException {
        return loadRelativePaths(directory, "data.json");
    }

    protected void saveDataDirectories(File directory,
            List<File> directories) throws LpException {
        saveRelativePaths(directory, "data.json", directories);
    }

    protected List<File> loadDebugDirectories(File directory)
            throws LpException {
        return loadRelativePaths(directory, "debug.json");
    }

    protected void saveDebugDirectories(File directory,
            List<File> directories) throws LpException {
        saveRelativePaths(directory, "debug.json", directories);
    }

    protected List<File> loadRelativePaths(File directory,
            String fileName) throws LpException {
        final Collection<String> directoriesAsString = loadCollectionFromJson(
                new File(directory, fileName), String.class);
        return fullAsFile(directoriesAsString, directory);
    }

    protected void saveRelativePaths(File directory, String fileName,
            List<File> directories) throws LpException {
        final Collection<String> directoriesAsString =
                relativeAsString(directories, directory);
        saveCollectionAsJson(new File(directory, fileName),
                directoriesAsString);
    }

    private List<String> relativeAsString(
            Collection<File> paths, File root) {
        Path rootPath = root.getAbsoluteFile().toPath();
        return paths.stream().map(
                (file) -> rootPath.relativize(file.toPath()).toString()
        ).collect(Collectors.toList());
    }

    private List<File> fullAsFile(Collection<String> paths, File root) {
        return paths.stream().map(
                (relativePath) -> new File(root, relativePath)
        ).collect(Collectors.toList());
    }

    private static void saveCollectionAsJson(
            File file, Collection<String> collection) throws LpException {
        final ObjectMapper mapper = new ObjectMapper();
        file.getParentFile().mkdirs();
        try {
            mapper.writeValue(file, collection);
        } catch (IOException ex) {
            throw new LpException("Can't save directory list.", ex);
        }
    }

    private static <T> Collection<T> loadCollectionFromJson(
            File file, Class<T> type) throws LpException {
        final ObjectMapper mapper = new ObjectMapper();
        final TypeFactory typeFactory = mapper.getTypeFactory();
        try {
            return mapper.readValue(file,
                    typeFactory.constructCollectionType(List.class, type));
        } catch (IOException ex) {
            throw new LpException("Can't load directory list.", ex);
        }
    }

}
