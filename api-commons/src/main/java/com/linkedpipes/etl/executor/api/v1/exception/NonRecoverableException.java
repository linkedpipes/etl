package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;

/**
 * This exception should terminate the execution.
 *
 * @author Petr Škoda
 */
public class NonRecoverableException extends LocalizedException {

    public NonRecoverableException(List<Message> messages,
            Object... args) {
        super(messages, args);
    }

}
