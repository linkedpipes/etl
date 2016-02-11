package com.linkedpipes.etl.executor.api.v1.plugin;

import com.linkedpipes.etl.executor.api.v1.event.Event;

/**
 *
 * @author Petr Škoda
 */
public interface MessageListener {

    /**
     * Called whenever message is published in the system.
     *
     * @param message
     */
    public void onMessage(Event message);

}
