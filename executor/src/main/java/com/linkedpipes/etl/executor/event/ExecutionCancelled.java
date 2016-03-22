package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

/**
 *
 * @author Petr Škoda
 */
class ExecutionCancelled extends AbstractEvent {

    ExecutionCancelled() {
        super(LINKEDPIPES.EVENTS.EXECUTION_CANCELLED, "Execution cancelled.");
    }

}
