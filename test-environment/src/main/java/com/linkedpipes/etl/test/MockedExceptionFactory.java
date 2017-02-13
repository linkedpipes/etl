package com.linkedpipes.etl.test;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MockedExceptionFactory implements ExceptionFactory {

    public static class MockedLpException extends LpException {

        public MockedLpException() {
            super("");
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(MockedExceptionFactory.class);

    @Override
    public LpException failure(String message, Object... args) {
        LOG.error("Exception: " + message, args);
        return new MockedLpException();
    }

}
