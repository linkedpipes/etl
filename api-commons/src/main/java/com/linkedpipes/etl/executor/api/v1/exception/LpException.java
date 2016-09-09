package com.linkedpipes.etl.executor.api.v1.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * LinkedPipes specific exception.
 *
 * @author Petr Škoda
 */
public class LpException extends Exception {

    /**
     * Store exception description.
     */
    protected final String message;

    /**
     * Store arguments for the message.
     */
    protected final Object[] args;

    protected LpException(String messages, Object... args) {
        // Extract cause if given.
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Throwable) {
                this.initCause((Exception) args[args.length - 1]);
            }
        }
        this.message = messages;
        this.args = args;
    }

    @Override
    public String getMessage() {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

}
