package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.RdfException;

/**
 * Exception factory that should be used in this project to create exceptions.
 *
 * TODO Use more specific exceptions.
 *
 * @author Petr Škoda
 */
final class ExceptionFactory {

    private ExceptionFactory() {
    }

    public static RdfException failure(String message, Object... args) {
        return RdfException.failure(message, args);
    }

    public static RdfException initializationFailed(String message,
            Object... args) {
        return RdfException.initializationFailed(message, args);
    }

}
