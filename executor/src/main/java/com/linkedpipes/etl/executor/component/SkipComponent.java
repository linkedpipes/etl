package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;

/**
 * Represent an execution of a component that should be skipped.
 */
class SkipComponent implements ComponentExecutor {

    @Override
    public void initialize(DataUnitManager dataUnitManager)
            throws ExecutorException {
        // No operation here.
    }

    @Override
    public void execute() throws ExecutorException {
        // No operation here.
    }

}
