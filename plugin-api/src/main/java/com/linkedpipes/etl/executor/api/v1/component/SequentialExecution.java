package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;

public interface SequentialExecution {

    /**
     * Perform execution of the component.
     */
    default void execute(Component.Context context) throws LpException {
        execute();
    }


    /**
     * Preserved for backward compatibility.
     */
    default void execute() throws LpException {

    }
}
