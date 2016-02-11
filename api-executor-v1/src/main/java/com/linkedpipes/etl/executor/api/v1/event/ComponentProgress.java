package com.linkedpipes.etl.executor.api.v1.event;

/**
 *
 * @author Škoda Petr
 */
public interface ComponentProgress extends Event {

    /**
     *
     * @return Null if total number is not known.
     */
    public Integer getTotal();

    public Integer getCurrent();

    public String getComponentUri();

}
