package com.linkedpipes.etl.executor.api.v1.context;

import com.linkedpipes.etl.executor.api.v1.event.Event;

/**
 *
 * @author Škoda Petr
 */
public interface Context {

    public void sendEvent(Event message);

}
