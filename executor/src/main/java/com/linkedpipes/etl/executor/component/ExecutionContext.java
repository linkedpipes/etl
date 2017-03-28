package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;

/**
 * Implementation of the context given to component for an execution.
 */
class ExecutionContext implements Component.Context {

    private final ExecutionObserver execution;

    private final ExecutionModel.Component component;

    private boolean cancelled = false;

    public ExecutionContext(
            ExecutionObserver execution,
            ExecutionModel.Component component) {
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
