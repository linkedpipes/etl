package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.dataunit.DataUnitManager;

/**
 * Represent an execution of a component that should be skipped.
 */
class SkipComponent implements ComponentExecutor {

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        return true;
    }

}
