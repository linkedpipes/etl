package com.linkedpipes.etl.rdf.utils;

import org.slf4j.helpers.MessageFormatter;

/**
 * Base exception used by RDF utils module.
 */
public class RdfUtilsException extends Exception {

    protected final String message;

    protected final Object[] args;

    public RdfUtilsException(String messages, Object... args) {
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
