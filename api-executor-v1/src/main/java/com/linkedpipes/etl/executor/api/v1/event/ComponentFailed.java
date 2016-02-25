package com.linkedpipes.etl.executor.api.v1.event;

/**
 *
 * @author Petr Škoda
 */
public interface ComponentFailed extends Event {

    public String getComponentUri();

}
