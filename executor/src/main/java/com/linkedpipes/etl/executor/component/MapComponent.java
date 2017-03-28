package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;

/**
 * Represent an execution of a component that is mapped from another
 * execution. Such component is not executed, however the references
 * to the data units from another execution must be created.
 */
class MapComponent implements ComponentExecutor {

    private final ExecutionObserver execution;

    private final ExecutionModel.Component execComponent;

    public MapComponent(ExecutionObserver execution,
            ExecutionModel.Component execComponent) {
        this.execution = execution;
        this.execComponent = execComponent;
    }

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        execution.onMapComponentBegin(execComponent);
        try {
            dataUnitManager.onComponentMapByReference(execComponent);
        } catch (ExecutorException ex) {
            execution.onMapComponentFailed(execComponent, ex);
            return false;
        }
        execution.onMapComponentSuccessful(execComponent);
        return true;
    }

}
