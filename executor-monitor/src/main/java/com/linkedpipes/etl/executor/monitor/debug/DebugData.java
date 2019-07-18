package com.linkedpipes.etl.executor.monitor.debug;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Holds information about debug data for a single execution.
 */
public class DebugData {

    private final String executionId;

    private final File executionDirectory;

    private final Map<String, DataUnit> dataUnits;

    public DebugData(
            String executionId,
            File executionDirectory,
            Map<String, DataUnit> dataUnits) {
        this.executionId = executionId;
        this.executionDirectory = executionDirectory;
        this.dataUnits = dataUnits;
    }

    public String getExecutionId() {
        return executionId;
    }

    public File getExecutionDirectory() {
        return executionDirectory;
    }

    public Map<String, DataUnit> getDataUnits() {
        return Collections.unmodifiableMap(dataUnits);
    }

}
