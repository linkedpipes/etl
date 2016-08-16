package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petr Škoda
 */
public final class EventFactory {

    private static final Logger LOG
            = LoggerFactory.getLogger(EventFactory.class);

    private EventFactory() {
    }

    public static Event componentBegin(PipelineModel.Component component) {
        LOG.info("componentBegin");
        return new ComponentBegin(component.getIri());
    }

    public static Event componentFailed(PipelineModel.Component component,
            Throwable throwable) {
        LOG.error("componentFailed", throwable);
        return new ComponentFailed(component.getIri(), throwable);
    }

    public static Event componentFinished(PipelineModel.Component component) {
        LOG.info("componentFinished");
        return new ComponentFinished(component.getIri());
    }

    public static Event executionBegin() {
        LOG.info("executionBegin");
        return new ExecutionBegin();
    }

    public static Event executionFinished() {
        LOG.info("executionFinished");
        return new ExecutionFinished();
    }

    public static Event executionFailed(String reason) {
        LOG.error("executionFailed: " + reason);
        return new ExecutionFailed(reason, null,
                LINKEDPIPES.EVENTS.EXECUTION_FAILED, "Execution failed.");
    }

    public static Event executionFailed(String reason,
            Throwable cause) {
        LOG.error("executionFailed: " + reason, cause);
        return new ExecutionFailed(reason, cause,
                LINKEDPIPES.EVENTS.EXECUTION_FAILED, "Execution failed.");
    }

    public static Event initializationFailed(String reason, Throwable cause) {
        LOG.error("initializationFailed: " + reason, cause);
        return new ExecutionFailed(reason, cause,
                LINKEDPIPES.EVENTS.INITIALIZATION_FAILED,
                "Initialization failed.");
    }
}
