package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.utils.core.event.boundary.AbstractEvent;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionCancelled extends AbstractEvent {

    public ExecutionCancelled() {
        super(LINKEDPIPES.EVENTS.EXECUTION_CANCELLED, "Execution cancelled.", "en");
    }

}
