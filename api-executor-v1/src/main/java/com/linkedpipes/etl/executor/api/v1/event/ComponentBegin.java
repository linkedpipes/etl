package com.linkedpipes.etl.executor.api.v1.event;

/**
 *
 * @author Škoda Petr
 */
public interface ComponentBegin extends Event {

    public String getComponentUri();

}
