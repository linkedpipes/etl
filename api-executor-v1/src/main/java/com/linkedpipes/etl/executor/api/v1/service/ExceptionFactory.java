package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;

/**
 * Can be used to create an exception that reports various failures.
 */
public interface ExceptionFactory {

    /**
     * Used for general exceptions. Use "{}" in the message to refer to
     * arguments.
     */
    LpException failure(String message, Object... args);

}
