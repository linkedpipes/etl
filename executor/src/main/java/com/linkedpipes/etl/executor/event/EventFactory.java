package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

/**
 *
 * @author Petr Škoda
 */
public final class EventFactory {

    private EventFactory() {
    }

    public static Event componentBegin(PipelineModel.Component component) {
        return new ComponentBegin(component.getIri());
    }

    public static Event componentFailed(PipelineModel.Component component,
            Throwable throwable) {
        return new ComponentFailed(component.getIri(), throwable);
    }

    public static Event componentFinished(PipelineModel.Component component) {
        return new ComponentFinished(component.getIri());
    }

    public static Event executionBegin() {
        return new ExecutionBegin();
    }

    public static Event executionFinished() {
        return new ExecutionFinished();
    }

    public static Event executionCancelled() {
        return new ExecutionCancelled();
    }

    public static Event executionFailed(String reason) {
        return new ExecutionFailed(reason, null,
                LINKEDPIPES.EVENTS.EXECUTION_FAILED, "Execution failed.");
    }

    public static Event executionFailed(String reason,
            Throwable cause) {
        return new ExecutionFailed(reason, cause,
                LINKEDPIPES.EVENTS.EXECUTION_FAILED, "Execution failed.");
    }

    public static Event initializationFailed(String reason) {
        return new ExecutionFailed(reason, null,
                LINKEDPIPES.EVENTS.INITIALIZATION_FAILED,
                "Initialization failed.");
    }

    public static Event initializationFailed(String reason, Throwable cause) {
        return new ExecutionFailed(reason, cause,
                LINKEDPIPES.EVENTS.INITIALIZATION_FAILED,
                "Initialization failed.");
    }
}
