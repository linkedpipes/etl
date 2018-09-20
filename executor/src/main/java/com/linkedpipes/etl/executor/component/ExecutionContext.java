package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;

/**
 * Implementation of the context given to component for an execution.
 */
class ExecutionContext implements Component.Context {

    private final ExecutionComponent component;

    private final ExecutionObserver execution;

    private boolean cancelled = false;

    public ExecutionContext(
            ExecutionComponent component, ExecutionObserver execution) {
        this.component = component;
        this.execution = execution;
    }

    @Override
    public void sendMessage(Event message) {
        this.execution.onComponentEvent(this.component, message);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

}
