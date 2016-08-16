package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

/**
 * Must be presented at the end of every execution even if execution failed
 * or was cancelled.
 *
 * @author Škoda Petr
 */
public class ExecutionFinished extends AbstractEvent {

    ExecutionFinished() {
        super(LINKEDPIPES.EVENTS.EXECUTION_END, "Execution finished.");
    }

}
