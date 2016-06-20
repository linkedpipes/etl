package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * TODO: Implement custom formats of ExecutionFailed with proper
 * support of serialisation into RDF.
 *
 * TODO: Check if given exception is not instance of RdfException.
 *
 * @author Petr Škoda
 */
class ExceptionFactoryImpl implements ExceptionFactory {

    private final String componentIri;

    ExceptionFactoryImpl(String componentIri) {
        this.componentIri = componentIri;
    }

    @Override
    public LpException failed(String message, Object... args) {
        return RdfException.componentFailed(message, args);
    }

    @Override
    public LpException invalidConfigurationProperty(String propertyIri,
            String message, Object... args) {
        return RdfException.invalidProperty(null, propertyIri, message, args);
    }

    @Override
    public LpException missingConfigurationProperty(String propertyIri) {
        return RdfException.missingProperty(null, propertyIri);
    }

}
