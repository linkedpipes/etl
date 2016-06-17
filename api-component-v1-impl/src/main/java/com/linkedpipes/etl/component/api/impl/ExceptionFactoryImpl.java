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

    @Override
    public ExecutionFailed invalidConfigurationProperty(String propertyIri,
            String message, Object... args) {
        final String mergedMessage = "Invalid configuration property <"
                + propertyIri + ">\n" + message;
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(mergedMessage, "en")),
                args);
    }

    @Override
    public ExecutionFailed missingConfigurationProperty(String propertyIri) {
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(
                        "Missing configuration property: {}", "en")),
                propertyIri);
    }



}
