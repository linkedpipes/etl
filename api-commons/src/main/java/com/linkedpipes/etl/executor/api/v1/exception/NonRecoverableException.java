package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * This exception should terminate the component execution.
 *
 * @author Petr Škoda
 */
public class NonRecoverableException extends Exception {

    public NonRecoverableException() {
    }

    public NonRecoverableException(String message) {
        super(message);
    }

    public NonRecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonRecoverableException(Throwable cause) {
        super(cause);
    }

}
