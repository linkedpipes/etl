package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Instance;
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
     * True if pipeline execution should continue.
     */
    boolean execute(DataUnitManager dataUnitManager);

    /**
     * Cancel the component execution.
     */
    default void cancel() {
        // By default, do nothing.
    }

    static ComponentExecutor create(
            Pipeline pipeline, ExecutionObserver execution,
            PipelineComponent component, PluginV1Instance instance)
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
            default:
                throw new ExecutorException("Unknown execution type: {} for {}",
                        component.getExecutionType(), component.getIri());
        }
    }

}
