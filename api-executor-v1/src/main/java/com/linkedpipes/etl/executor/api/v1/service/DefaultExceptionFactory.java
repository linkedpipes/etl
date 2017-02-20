package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;

/**
 * Default implementation of exception factory.
 */
class DefaultExceptionFactory implements ExceptionFactory {

    private final String component;

    public DefaultExceptionFactory(String componentIri) {
        this.component = componentIri;
    }

    @Override
    public LpException failure(String message, Object... args) {
        return new LpException(message, args);
    }

}
