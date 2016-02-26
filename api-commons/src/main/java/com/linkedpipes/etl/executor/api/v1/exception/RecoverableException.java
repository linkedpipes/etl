package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;

/**
 * This exception indicate a possible temporary problem and component may try to re-execute the code.
 *
 * @author Petr Škoda
 */
public class RecoverableException extends LocalizedException {

    public RecoverableException(List<LocalizedString> messages, Object... args) {
        super(messages, args);
    }

}
