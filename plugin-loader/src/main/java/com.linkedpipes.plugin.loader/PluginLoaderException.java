package com.linkedpipes.plugin.loader;

import org.slf4j.helpers.MessageFormatter;

public class PluginLoaderException extends Exception {

    protected final String message;

    protected final Object[] args;

    public PluginLoaderException(String messages, Object... args) {
        this.message = messages;
        this.args = args;
        extractCause(args);
    }

    private void extractCause(Object... args) {
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Throwable) {
                this.initCause((Throwable) args[args.length - 1]);
            }
        }
    }

    @Override
    public String getMessage() {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

}
