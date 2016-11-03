package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

class ExceptionFactoryImpl implements ExceptionFactory {

    private final String componentIri;

    ExceptionFactoryImpl(String componentIri) {
        this.componentIri = componentIri;
    }

    @Override
    public LpException failure(String message, Object... args) {
        return RdfException.componentFailed(message, args);
    }

}
