package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.component.BaseComponent;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.event.EventManager;

/**
 * Implementation of a component context.
 *
 * @author Petr Škoda
 */
class ComponentContext implements BaseComponent.Context {

    private final EventManager eventManager;

    private boolean cancelled = false;

    ComponentContext(EventManager manager) {
        this.eventManager = manager;
    }

    void cancell() {
        cancelled = true;
    }

    @Override
    public boolean canceled() {
        return cancelled;
    }

    @Override
    public void sendMessage(Event event) {
        eventManager.publish(event);
    }

}
