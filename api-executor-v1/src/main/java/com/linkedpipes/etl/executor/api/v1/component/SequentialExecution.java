package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;

public interface SequentialExecution {

    /**
     * Perform execution of the component.
     */
    void execute() throws LpException;

}
