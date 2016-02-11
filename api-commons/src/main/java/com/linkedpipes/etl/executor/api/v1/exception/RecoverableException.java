package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * This exception indicate a possible temporary problem and component may try to re-execute the code.
 *
 * @author Petr Škoda
 */
public class RecoverableException extends Exception {

    public RecoverableException() {
    }

    public RecoverableException(String message) {
        super(message);
    }

    public RecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecoverableException(Throwable cause) {
        super(cause);
    }

}
