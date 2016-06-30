package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * LinkedPipes specific exception.
 *
 * @author Petr Škoda
 */
public class LpException extends Exception {

    protected LpException() {
    }

    protected LpException(String message) {
        super(message);
    }

    protected LpException(String message, Throwable cause) {
        super(message, cause);
    }

}
