package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

public class ExecutionBegin extends AbstractEvent {

    ExecutionBegin() {
        super(LINKEDPIPES.EVENTS.EXECUTION_BEGIN, "Execution started.");
    }

}
