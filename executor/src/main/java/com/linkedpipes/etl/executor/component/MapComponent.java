package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

/**
 * Represent an execution of a component that is mapped from another
 * execution. Such component is not executed, however the references
 * to the data units from another execution must be created.
 */
class MapComponent implements ComponentExecutor {

    private final PipelineModel.Component component;

    public MapComponent(PipelineModel.Component component) {
        this.component = component;
    }

    @Override
    public void initialize(DataUnitManager dataUnitManager)
            throws ExecutorException {
        dataUnitManager.onMappedComponent(component);
    }

    @Override
    public void execute() throws ExecutorException {
        // No operation here.
    }

}
