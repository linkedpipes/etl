package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.execution.Execution;

/**
 * Represents an instance of an executor.
 */
public class Executor {

    /**
     * Executor base address.
     */
    private final String address;

    /**
     * IRI of execution assigned to the executor.
     */
    private Execution execution = null;

    /**
     * True is executor is alive and responsive.
     */
    private boolean alive = false;

    Executor(String address) {
        this.address = address;
    }

    String getAddress() {
        return address;
    }

    public Execution getExecution() {
        return execution;
    }

    void setExecution(Execution execution) {
        this.execution = execution;
    }

    boolean isAlive() {
        return alive;
    }

    void setAlive(boolean alive) {
        this.alive = alive;
    }

    boolean isAvailableForNewExecution() {
        return alive && execution == null;
    }

}
