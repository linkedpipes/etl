package com.linkedpipes.etl.executor.api.v1;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * Specification of the {@link LpException} as used in this module.
 */
public class RdfException extends LpException {

    protected RdfException(String messages, Object... args) {
        super(messages, args);
    }

    /**
     * @param ex
     * @return Return wrap of given {@link LpException}.
     */
    public static RdfException rethrow(LpException ex) {
        if (ex instanceof RdfException) {
            return (RdfException) ex;
        } else {
            return failure("", ex);
        }
    }

    /**
     * Create and return exception that reports general failure.
     *
     * @param message
     * @param args
     * @return
     */
    public static RdfException failure(String message, Object... args) {
        return new RdfException(message, args);
    }

    /**
     * Create and return exception that reports missing property of RDF
     * resource.
     *
     * @param resource
     * @param property
     * @return
     */
    public static RdfException missingProperty(String resource,
            String property) {
        if (resource == null) {
            return new RdfException("Missing property: {}", property);
        } else {
            return new RdfException("Missing property: {} on {}",
                    property, resource);
        }
    }

    /**
     * Create and return exception that reports invalid property on RDF
     * resource.
     *
     * @param resource
     * @param property
     * @return
     */
    public static RdfException invalidProperty(String resource,
            String property, String message) {
        if (resource == null) {
            return new RdfException("Invalid property: {}\n\t{}", property,
                    message);
        } else {
            return new RdfException("Invalid property: {} on {}\n\t{}",
                    property, resource, message);
        }
    }

    /**
     * Create and return exception that reports failure of a component.
     *
     * @param message
     * @param args
     * @return
     */
    public static RdfException componentFailed(String message, Object... args) {
        return new RdfException(message, args);
    }

    /**
     * Create and return exception that reports problem with data unit.
     *
     * @param binding
     * @return
     */
    public static RdfException problemWithDataUnit(String binding) {
        return new RdfException("Problem with data unit: {}", binding);
    }

    /**
     * Create and return exception that reports problem with data unit.
     *
     * @param binding
     * @return
     */
    public static RdfException problemWithDataUnit(String binding,
            Exception ex) {
        return new RdfException("Problem with data unit: {}", binding, ex);
    }

    /**
     * Create and return exception that reports problem with initialization.
     *
     * @param message
     * @param args
     * @return
     */
    public static RdfException initializationFailed(String message,
            Object... args) {
        return new RdfException("Initialization failed: " + message, args);
    }

}
