package com.linkedpipes.etl.executor.api.v1;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.slf4j.helpers.MessageFormatter;

/**
 * Basic interface for exception.
 *
 * @author Petr Škoda
 */
public class RdfException extends LpException {

    private final String message;

    private final Object[] args;

    private Throwable cause = null;

    protected RdfException(String messages, Object... args) {
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

    public static RdfException rethrow(LpException ex) {
        if (ex instanceof RdfException) {
            return (RdfException)ex;
        } else {
            return failure("", ex);
        }
    }

    public static RdfException failure(String message, Object... args) {
        return new RdfException(message, args);
    }

    public static RdfException wrap(RdfException exception,
            String message, Object... args) {
        return new RdfException(message, args);
    }

    public static RdfException wrap(LpException exception,
            String message, Object... args) {
        return new RdfException(message, args);
    }

    public static RdfException missingProperty(String resource,
            String property) {
        if (resource == null) {
            return new RdfException("Missing property: {}", property);
        } else {
            return new RdfException("Missing property: {} on {}",
                    property, resource);
        }
    }

    public static RdfException invalidProperty(String resource,
            String property, String message, Object... args) {
        if (resource == null) {
            return new RdfException("Invalid property: {}", property);
        } else {
            return new RdfException("Invalid property: {} on {}",
                    property, resource);
        }
    }

    public static RdfException cantCreateObject(String message, Object... args) {
        return new RdfException(message, args);
    }

    public static RdfException componentFailed(String message, Object... args) {
        return new RdfException(message, args);
    }

    public static RdfException problemWithDataUnit(String binding) {
        return new RdfException("Problem with data unit: {}", binding);
    }

    public static RdfException problemWithDataUnit(String binding, Exception ex) {
        return new RdfException("Problem with data unit: {}", binding, ex);
    }

    public static RdfException initializationFailed(String message, Object... args) {
        return new RdfException("Initialization failed: " + message, args);
    }

    public static RdfException shutdownFailed(String message, Object... args) {
        return new RdfException("Can't save object: " + message, args);
    }

}
