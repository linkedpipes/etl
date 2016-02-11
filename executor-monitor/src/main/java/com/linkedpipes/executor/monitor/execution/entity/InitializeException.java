package com.linkedpipes.executor.monitor.execution.entity;

/**
 *
 * @author Petr Škoda
 */
public class InitializeException extends Exception {

    public InitializeException(String message) {
        super(message);
    }

    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

}
