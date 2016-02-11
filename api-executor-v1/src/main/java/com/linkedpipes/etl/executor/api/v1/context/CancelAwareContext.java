package com.linkedpipes.etl.executor.api.v1.context;

/**
 *
 * @author Škoda Petr
 */
public interface CancelAwareContext extends Context {

    /**
     *
     * @return True if component should stop (fail) as soon as possible.
     */
    public boolean canceled();

}
