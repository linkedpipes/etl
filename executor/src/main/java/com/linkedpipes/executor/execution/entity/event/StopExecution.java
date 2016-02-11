package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.utils.core.event.boundary.AbstractEvent;

/**
 * Used to stop the execution before executing next component.
 *
 * @author Škoda Petr
 */
public class StopExecution extends AbstractEvent {

    public StopExecution() {
        super(LINKEDPIPES.EVENTS.EXECUTION_STOP, "Execution stopped.", "en");
    }

}
