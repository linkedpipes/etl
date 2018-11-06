package com.linkedpipes.etl.executor.web.servlet;

import com.linkedpipes.etl.executor.ExecutorException;

public class MissingResource extends ExecutorException {

    public MissingResource(String message, Object... args) {
        super(message, args);
    }

}
