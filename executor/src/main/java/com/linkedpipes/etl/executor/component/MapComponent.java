package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

/**
 * Represent an execution of a component that is mapped from another
 * execution. Such component is not executed, however the references
 * to the data units from another execution must be created.
 */
class MapComponent implements ComponentExecutor {

    private final Execution execution;

    private final Execution.Component execComponent;

    public MapComponent(Execution execution,
            PipelineModel.Component component) {
        this.execution = execution;
        this.execComponent = execution.getComponent(component);
    }

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        try {
            dataUnitManager.onComponentWillExecute(execComponent);
        } catch (ExecutorException ex) {
            execution.onCantPrepareDataUnits(execComponent, ex);
            return false;
        }
        try {
            dataUnitManager.onComponentDidExecute(execComponent);
        } catch (ExecutorException ex) {
            execution.onCantSaveDataUnits(execComponent, ex);
            return false;
        }
        execution.onComponentMapped(execComponent);
        return true;
    }

}
