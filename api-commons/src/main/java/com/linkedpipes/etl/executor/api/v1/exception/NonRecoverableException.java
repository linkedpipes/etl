package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * This exception should terminate the component execution.
 *
 * The reference of arguments in message should by done by '{}' string. The cause exception should be given
 * as the last argument, if the cause exception is available.
 *
 * @author Petr Škoda
 */
public class NonRecoverableException extends Exception {

    private final Object[] args;

    public NonRecoverableException(String message, Object... args) {
        super(message);
        this.args = args;
    }

}
