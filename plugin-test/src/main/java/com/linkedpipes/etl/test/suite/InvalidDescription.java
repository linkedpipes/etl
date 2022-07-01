package com.linkedpipes.etl.test.suite;

import com.linkedpipes.etl.executor.api.v1.LpException;

public class InvalidDescription extends LpException {

    public InvalidDescription(String messages, Object... args) {
        super(messages, args);
    }

}
