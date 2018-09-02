package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.pipeline.model.Port;

import java.io.File;

/**
 * Represent information about data unit in a execution.
 */
public class DataUnit {

    private final Port port;

    private final String debugVirtualPathSuffix;

    private final File saveDirectory;

    private final File loadDirectory;

    private final String relativeSaveDataPath;

    public DataUnit(
            Port dataUnit,
            String debugVirtualPathSuffix,
            File saveDirectory,
            File loadDirectory,
            String relativeDataPath) {
        this.port = dataUnit;
        this.debugVirtualPathSuffix = debugVirtualPathSuffix;
        this.saveDirectory = saveDirectory;
        this.loadDirectory = loadDirectory;
        this.relativeSaveDataPath = relativeDataPath;
    }

    public DataUnit(
            Port dataUnit,
            File loadDirectory) {
        this.port = dataUnit;
        this.debugVirtualPathSuffix = null;
        this.saveDirectory = null;
        this.loadDirectory = loadDirectory;
        this.relativeSaveDataPath = null;
    }

    public String getIri() {
        return this.port.getIri();
    }

    public String getVirtualDebugPath() {
        return this.debugVirtualPathSuffix;
    }

    public File getLoadDirectory() {
        return this.loadDirectory;
    }

    public File getSaveDirectory() {
        return this.saveDirectory;
    }

    public String getRelativeSaveDataPath() {
        return this.relativeSaveDataPath;
    }

    public Port getPort() {
        return this.port;
    }

}
