package com.linkedpipes.etl.executor.monitor.debug;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataUnit {

    private static final Logger LOG = LoggerFactory.getLogger(DataUnit.class);

    private static final String DEBUG_FILE_NAME = "debug.json";

    private String name;

    private String relativeDataPath;

    private String mappedFromExecution;

    private List<File> debugDirectories = Collections.emptyList();

    private boolean loaded = false;

    DataUnit() {
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setRelativeDataPath(String relativeDataPath) {
        this.relativeDataPath = relativeDataPath;
    }

    public String getExecutionId() {
        if (this.mappedFromExecution == null) {
            return null;
        }
        String iri = this.mappedFromExecution;
        return iri.substring(iri.indexOf("executions/") + 11);
    }

    void setMappedFromExecution(String execution) {
        this.mappedFromExecution = execution;
    }

    public List<File> getDebugDirectories() {
        return Collections.unmodifiableList(this.debugDirectories);
    }

    /**
     * Given path must belong to {@link #mappedFromExecution}.
     */
    public void updateDebugDirectories(File executionDirectory) {
        if (this.loaded) {
            return;
        }
        File debugInfoFile = getDebugInfoFile(executionDirectory);
        List<String> relativePaths;
        try {
            relativePaths = loadJsonList(debugInfoFile);
        } catch (IOException ex) {
            this.debugDirectories = Collections.emptyList();
            LOG.error("Can't read data unit debug file: {}", debugInfoFile, ex);
            return;
        }
        this.loaded = true;
        updateDebugDirectories(executionDirectory, relativePaths);
    }

    private File getDebugInfoFile(File directory) {
        return new File(
                directory,
                this.relativeDataPath + File.separator + DEBUG_FILE_NAME);
    }

    private List<String> loadJsonList(File path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory()
                .constructCollectionType(List.class, String.class);
        return mapper.readValue(path, type);
    }

    private void updateDebugDirectories(File root, List<String> relativePaths) {
        File dataUnitRoot = new File(root, this.relativeDataPath);
        this.debugDirectories =
                relativePaths.stream()
                .map((path) -> new File(dataUnitRoot, path))
                .collect(Collectors.toList());
    }

}
