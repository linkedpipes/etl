package com.linkedpipes.etl.component.api.service;

import com.linkedpipes.etl.component.api.ExecutionFailed;

/**
 * Can be used to create an exception that reports various failures.
 *
 * @author Petr Škoda
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
    public ExecutionFailed failed(String message, Object... args);


}
