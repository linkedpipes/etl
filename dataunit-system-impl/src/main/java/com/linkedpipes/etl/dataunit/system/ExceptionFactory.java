package com.linkedpipes.etl.dataunit.system;

import com.linkedpipes.etl.executor.api.v1.RdfException;

/**
 *
 * @author Petr Škoda
 */
final class ExceptionFactory {

    private ExceptionFactory() {
    }

    public static RdfException failure(String message,
            Object... args) {
        return RdfException.failure(message, args);
    }

    public static RdfException initializationFailed(String message,
            Object... args) {
        return RdfException.initializationFailed(message, args);
    }

}
