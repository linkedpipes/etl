package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.Component.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import java.util.Arrays;

/**
 *
 * @author Petr Škoda
 */
class ExceptionFactoryImpl implements ExceptionFactory {

    @Override
    public ExecutionFailed failed(String message, Object... args) {
        return new Component.ExecutionFailed(Arrays.asList(
                new ExecutionFailed.LocalizedString(message, "en")),
                args);
    }

}
