package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;

public class RdfException extends LpException {

    public RdfException(String messages, Object... args) {
        super(messages, args);
    }

}
