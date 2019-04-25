package com.linkedpipes.etl.executor.monitor.debug;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Holds information about debug data for a single execution.
 */
public class DebugData {

    private final String execution;

    private final File executionDirectory;

    private final Map<String, DataUnit> dataUnits;

    public DebugData(
            String execution,
            File executionDirectory,
            Map<String, DataUnit> dataUnits) {
        this.execution = execution;
        this.executionDirectory = executionDirectory;
        this.dataUnits = dataUnits;
    }

    public String getExecution() {
        return execution;
    }

    public File getExecutionDirectory() {
        return executionDirectory;
    }

    public Map<String, DataUnit> getDataUnits() {
        return Collections.unmodifiableMap(dataUnits);
    }

}
