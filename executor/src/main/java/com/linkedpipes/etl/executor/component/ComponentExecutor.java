package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;

/**
 * Interface of component executor. The component executor is responsible
 * for execution of a component in given way.
 */
public interface ComponentExecutor {

    /**
     * @param dataUnitManager
     * @return True if pipeline can continue
     */
    boolean execute(DataUnitManager dataUnitManager);

    /**
     * Cancel the component execution.
     */
    default void cancel() {
        // By default do nothing.
    }

    /**
     * @param pipeline
     * @param execution
     * @param component
     * @param instance PipelineComponent instance, can be null.
     * @return Never return null.
     */
    static ComponentExecutor create(
            Pipeline pipeline, ExecutionObserver execution,
            PipelineComponent component, ManageableComponent instance)
            throws ExecutorException {
        ExecutionComponent execComponent =
                execution.getModel().getComponent(component);
        switch (component.getExecutionType()) {
            case EXECUTE:
                return new ExecuteComponent(pipeline, execution,
                        component, execComponent, instance);
            case MAP:
                return new MapComponent(execution, execComponent);
            case SKIP:
                return new SkipComponent();
        }
        throw new ExecutorException("Unknown execution type: {} for {}",
                component.getExecutionType(), component.getIri());
    }

}
