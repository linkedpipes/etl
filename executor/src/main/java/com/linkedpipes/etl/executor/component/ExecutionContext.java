package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.Execution;

/**
 * Implementation of the context given to component for an execution.
 */
class ExecutionContext implements Component.Context {

    private final Execution execution;

    private final Execution.Component component;

    private boolean cancelled = false;

    public ExecutionContext(
            Execution execution,
            Execution.Component component) {
        this.execution = execution;
        this.component = component;
    }

    @Override
    public void sendMessage(Event message) {
        execution.onEvent(component, message);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

}
