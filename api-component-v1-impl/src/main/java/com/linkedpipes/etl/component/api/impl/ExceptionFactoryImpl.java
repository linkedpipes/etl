package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import java.util.Arrays;

/**
 * TODO: Implement custom formats of ExecutionFailed with proper
 * support of serialisation into RDF.
 *
 * @author Petr Škoda
 */
class ExceptionFactoryImpl implements ExceptionFactory {

    @Override
    public ExecutionFailed failed(String message, Object... args) {
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(message, "en")),
                args);
    }


}
