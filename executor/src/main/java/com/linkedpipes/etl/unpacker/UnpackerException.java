package com.linkedpipes.etl.unpacker;

import org.slf4j.helpers.MessageFormatter;

/**
 * A base exception used by the unpacker component.
 */
public class UnpackerException extends Exception {

    private final String message;

    private final Object[] args;

    private Throwable cause = null;

    public UnpackerException(String message, Object... args) {
        // Initialize exception.
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Exception) {
                this.cause = ((Exception) args[args.length - 1]);
            }
        }
        this.message = message;
        this.args = args;
    }

    public UnpackerException(Throwable cause) {
        this.message = "";
        this.args = new Object[0];
        this.cause = cause;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        // Use first given message if it exists.
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }
}
