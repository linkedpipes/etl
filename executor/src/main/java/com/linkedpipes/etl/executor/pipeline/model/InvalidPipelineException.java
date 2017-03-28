package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.ExecutorException;

public class InvalidPipelineException extends ExecutorException {

    public InvalidPipelineException(String messages, Object... args) {
        super(messages, args);
    }

}
