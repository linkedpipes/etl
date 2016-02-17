package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * This exception indicate a possible temporary problem and component may try to re-execute the code.
 *
 * The reference of arguments in message should by done by '{}' string. The cause exception should be given
 * as the last argument, if the cause exception is available.
 *
 * @author Petr Škoda
 */
public class RecoverableException extends Exception {

    private final Object[] args;

    public RecoverableException(String message, Object... args) {
        super(message);
        this.args = args;
    }

}
