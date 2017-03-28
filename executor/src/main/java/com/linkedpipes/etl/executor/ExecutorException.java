package com.linkedpipes.etl.executor;

import com.linkedpipes.etl.executor.api.v1.LpException;

/**
 * Base exception used in the executor.
 */
public class ExecutorException extends LpException {

    public ExecutorException(String messages, Object... args) {
        super(messages, args);
    }

}
