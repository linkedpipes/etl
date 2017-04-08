package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.LpException;

public class MissingPortException extends LpException {

    public MissingPortException(String port) {
        super("Missing port: {}", port);
    }
}

