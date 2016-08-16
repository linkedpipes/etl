package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class MockedExceptionFactory implements ExceptionFactory {

    public static class MockedLpException extends LpException {

        public MockedLpException(String message) {
            super(message);
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(MockedExceptionFactory.class);

    @Override
    public LpException failure(String message, Object... args) {
        LOG.error("Exception: " + message, args);
        return new MockedLpException("failure");
    }

    @Override
    public LpException invalidRdfProperty(
            String propertyIri, String message, Object... args) {
        LOG.error("Invalid configuration property: {}", propertyIri);
        return new MockedLpException("invalidRdfProperty");
    }

    @Override
    public LpException missingRdfProperty(
            String propertyIri) {
        LOG.error("Missing configuration property: {}", propertyIri);
        return new MockedLpException("missingRdfProperty");
    }

}
