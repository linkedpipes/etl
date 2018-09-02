package com.linkedpipes.etl.executor.monitor.debug;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataUnit {

    private static final Logger LOG = LoggerFactory.getLogger(DataUnit.class);

    /**
     * Name of this data unit used for debug.
     */
    private String debugName;

    /**
     * Relative path to data directory.
     */
    private String dataPath;

    /**
     * If not null then this data unit is mapped from another
     * executionId.
     */
    private String execution;

    /**
     * List of debugging directories.
     */
    private List<File> debugDirectories = Collections.EMPTY_LIST;

    private boolean loaded = false;

    DataUnit() {
    }

    public DataUnit(String debug) {
        this.debugName = debug;
    }

    public String getDebugName() {
        return debugName;
    }

    void setDebugName(String debugName) {
        this.debugName = debugName;
    }

    public String getDataPath() {
        return dataPath;
    }

    void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getExecutionId() {
        if (execution == null) {
            return null;
        } else {
            return execution.substring(
                    execution.indexOf("executions/") + 11);
        }
    }

    void setExecution(String execution) {
        this.execution = execution;
    }

    public List<File> getDebugDirectories() {
        return Collections.unmodifiableList(debugDirectories);
    }

    /**
     * If {@link #debugDirectories} are not set then load them.
     * Given execution must match the {@link #execution}.
     *
     * @param executionDirectory
     */
    public void loadDebugDirectories(File executionDirectory) {
        if (loaded) {
            // Already loaded.
            return;
        }
        final ObjectMapper mapper = new ObjectMapper();
        final JavaType type = mapper.getTypeFactory()
                .constructCollectionType(List.class, String.class);
        //
        final File inputFile = new File(executionDirectory,
                dataPath + "/debug.json");
        final List<String> relativePaths;
        try {
            relativePaths = mapper.readValue(inputFile, type);
            this.debugDirectories = new ArrayList<>(relativePaths.size());
            for (String item : relativePaths) {
                this.debugDirectories.add(new File(executionDirectory,
                        dataPath + File.separator + item));
            }
            // If we fail we don't mark the data as loaded,
            // thus next time we will try to load again.
            // The file may not be ready in time of call of this object,
            // but could be created later.
            loaded = true;
        } catch (IOException ex) {
            this.debugDirectories = Collections.EMPTY_LIST;
            LOG.error("Can't read data unit debug file from: {}", inputFile, ex);
        }
    }

}
