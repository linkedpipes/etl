package com.linkedpipes.etl.component.api;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
public class ExecutionFailed extends NonRecoverableException {

    public ExecutionFailed(List<LocalizedException.Message> messages,
            Object... args) {
        super(messages, args);
    }

}
