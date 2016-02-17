package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;

/**
 * This exception should terminate the component execution.
 *
 * The reference of arguments in message should by done by '{}' string. The cause exception should be given
 * as the last argument, if the cause exception is available.
 *
 * @author Petr Škoda
 */
public class NonRecoverableException extends Exception {

    /**
     * For each language a message with placeholders for arguments.
     */
    protected final List<LocalizedString> messages;

    protected final Object[] args;

    public NonRecoverableException(List<LocalizedString> messages, Object... args) {
        this.messages = messages;
        this.args = args;
    }

}
