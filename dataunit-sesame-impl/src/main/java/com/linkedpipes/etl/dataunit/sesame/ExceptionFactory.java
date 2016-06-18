package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * Exception factory that should be used in this project to create exceptions.
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

    public static RdfException wrap(LpException ex, String message,
            Object ...args) {
        return RdfException.wrap(ex, message, args);
    }

}
