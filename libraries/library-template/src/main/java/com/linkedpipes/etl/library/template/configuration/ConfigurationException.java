package com.linkedpipes.etl.library.template.configuration;

import org.slf4j.helpers.MessageFormatter;

public class ConfigurationException extends Exception {

    private final String message;

    private final Object[] args;

    private Throwable cause = null;

    public ConfigurationException(String message, Object... args) {
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Exception) {
                this.cause = ((Exception) args[args.length - 1]);
            }
        }
        this.message = message;
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
