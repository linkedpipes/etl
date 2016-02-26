package com.linkedpipes.executor.execution.contoller;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.executor.rdf.boundary.MessageStorage;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;

/**
 *
 * @author Škoda Petr
 */
final class CancelAwareContextImpl implements ExecutionContext {

    private final MessageStorage messageStorage;

    private boolean cancel = false;

    public CancelAwareContextImpl(MessageStorage messageStorage) {
        this.messageStorage = messageStorage;
    }

    @Override
    public boolean canceled() {
        return cancel;
    }

    @Override
    public void sendMessage(Event message) {
        messageStorage.publish(message);
    }

    /**
     * Set cancel flag to true.
     */
    public void cancel() {
        this.cancel = true;
    }

}
