package com.linkedpipes.etl.executor.module;

import com.linkedpipes.etl.executor.ExecutorException;

public class ModuleException extends ExecutorException {

    public ModuleException(String messages, Object... args) {
        super(messages, args);
    }
}
