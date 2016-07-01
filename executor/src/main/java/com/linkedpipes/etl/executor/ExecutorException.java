package com.linkedpipes.etl.executor;

import org.slf4j.helpers.MessageFormatter;

/**
 * A base exception.
 *
 * @author Petr Škoda
 */
public class ExecutorException extends Exception {

    private final String message;

    private final Object[] args;

    private Throwable cause = null;

    protected ExecutorException(String messages, Object... args) {
        // Initialize exception.
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Exception) {
                this.cause = ((Exception) args[args.length - 1]);
            }
        }
        this.message = messages;
        this.args = args;
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
