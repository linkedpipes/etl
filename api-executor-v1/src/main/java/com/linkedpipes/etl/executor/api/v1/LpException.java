package com.linkedpipes.etl.executor.api.v1;

import org.slf4j.helpers.MessageFormatter;

/**
 * LinkedPipes specific exception.
 */
public class LpException extends Exception {

    protected final String message;

    protected final Object[] args;

    public LpException(String messages, Object... args) {
        // Extract cause if given.
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Throwable) {
                this.initCause((Throwable) args[args.length - 1]);
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
