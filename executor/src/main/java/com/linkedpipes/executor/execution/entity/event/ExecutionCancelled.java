package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.utils.core.event.AbstractEvent;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionCancelled extends AbstractEvent {

    public ExecutionCancelled() {
        super(LINKEDPIPES.EVENTS.EXECUTION_CANCELLED, "Execution cancelled.", "en");
    }

}
