package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

/**
 * Represent an execution of a component that should be skipped.
 */
class SkipComponent implements ComponentExecutor {

    private final Execution execution;

    private final Execution.Component execComponent;

    public SkipComponent(Execution execution,
            PipelineModel.Component component) {
        this.execution = execution;
        this.execComponent = execution.getComponent(component);
    }

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        execution.onComponentSkipped(execComponent);
        return true;
    }

}
