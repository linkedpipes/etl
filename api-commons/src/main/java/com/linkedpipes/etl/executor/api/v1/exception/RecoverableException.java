package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;

/**
 * This exception indicate a possible temporary problem that may be solved
 * by re-execution of the code.
 *
 * @author Petr Škoda
 */
public class RecoverableException extends LocalizedException {

    public RecoverableException(List<Message> messages,
            Object... args) {
        super(messages, args);
    }

}
