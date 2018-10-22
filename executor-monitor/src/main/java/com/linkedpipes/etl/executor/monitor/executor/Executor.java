package com.linkedpipes.etl.executor.monitor.executor;

/**
 * Represents an instance of an executor.
 */
public class Executor {

    private final String address;

    private boolean alive = false;

    Executor(String address) {
        this.address = address;
    }

    String getAddress() {
        return address;
    }

    boolean isAlive() {
        return alive;
    }

    void setAlive(boolean alive) {
        this.alive = alive;
    }

}
