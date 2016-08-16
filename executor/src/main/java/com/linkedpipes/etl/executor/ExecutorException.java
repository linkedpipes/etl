package com.linkedpipes.etl.executor;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 * A base exception.
 *
 * @author Petr Škoda
 */
public class ExecutorException extends LpException {

    protected ExecutorException(String messages, Object... args) {
        super(messages, args);
    }

}
