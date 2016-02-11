package com.linkedpipes.executor.monitor.process.entity;

import com.linkedpipes.commons.entities.executor.monitor.ExternalProcess;

/**
 *
 * @author Petr Škoda
 */
public abstract class BaseProcess {

    protected final ExternalProcess externalProcess;

    protected final Process systemProcess;

    /**
     * Used port.
     */
    protected final Integer port;

    public BaseProcess(ExternalProcess externalProcess, Process systemProcess, Integer port) {
        this.externalProcess = externalProcess;
        this.systemProcess = systemProcess;
        this.port = port;
    }

    public ExternalProcess getExternalProcess() {
        return externalProcess;
    }

    public Integer getPort() {
        return port;
    }

    public abstract void terminate();

}
