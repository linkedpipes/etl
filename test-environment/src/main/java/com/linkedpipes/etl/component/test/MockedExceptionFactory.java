package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class MockedExceptionFactory implements ExceptionFactory {

    private static final Logger LOG
            = LoggerFactory.getLogger(MockedExceptionFactory.class);

    @Override
    public Component.ExecutionFailed failed(String message, Object... args) {
        LOG.error("Exception: " + message, args);
        return new Component.ExecutionFailed(Arrays.asList(
                new Component.ExecutionFailed.LocalizedString(message, "en")),
                args);
    }

}
