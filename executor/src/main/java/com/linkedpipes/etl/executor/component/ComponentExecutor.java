package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.executor.api.v1.component.SimpleComponent;

/**
 * Interface and factory for component execution objects.
 *
 * @author Petr Škoda
 */
public interface ComponentExecutor {

    public void execute();

    /**
     * Request execution to stop as soon as possible.
     */
    public void cancel();

    /**
     * If true then the pipeline execution should fail as the executor
     * thread fail to end properly.
     *
     * @return True in case of unexpected termination.
     */
    public boolean unexpectedTermination();

    /**
     *
     * @param module
     * @param dataunit
     * @param events
     * @param pipeline
     * @param execution
     * @param componentIri
     * @param instance
     * @return Null if no executor for given component can be created.
     */
    public static ComponentExecutor create(ModuleFacade module,
            DataUnitManager dataunit, EventManager events,
            PipelineDefinition pipeline, ExecutionModel execution,
            String componentIri, SimpleComponent instance) {
        final PipelineModel.Component component
                = pipeline.getPipelineModel().getComponent(componentIri);
        final ExecutionModel.Component executionComponent
                = execution.getComponent(componentIri);
        if (component == null || executionComponent == null) {
            return null;
        }
        switch (component.getExecutionType()) {
            case EXECUTE:
                return new ExecuteComponent(instance, executionComponent,
                        component, dataunit, events);
            case MAP:
                return new MapComponent(events, dataunit, executionComponent);
            case SKIP:
                return new SkipComponent(executionComponent);
            default:
                return null;
        }
    }

}
