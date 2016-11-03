package com.linkedpipes.etl.component.api.service;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * Can be used to create an exception that reports various failures.
 */
public interface ExceptionFactory {

    /**
     * Used for general exceptions. Use "{}" in the message to refer to
     * arguments.
     *
     * @param message
     * @param args
     * @return
     */
    public LpException failure(String message, Object... args);

}
