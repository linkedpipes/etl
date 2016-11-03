package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.execution.Execution;

import java.util.Date;

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
    private Execution execution;

    /**
     * Time of last successful check with executor.
     */
    private Date lastCheck;

    /**
     * True is executor is alive and responsive.
     */
    private boolean alive = false;

    Executor(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public Execution getExecution() {
        return execution;
    }

    void setExecution(Execution execution) {
        this.execution = execution;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    public boolean isAlive() {
        return alive;
    }

    void setAlive(boolean alive) {
        this.alive = alive;
    }

}
