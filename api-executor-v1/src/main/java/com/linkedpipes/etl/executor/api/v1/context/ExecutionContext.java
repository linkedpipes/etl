package com.linkedpipes.etl.executor.api.v1.context;

/**
 * Given to the component upon execution.
 *
 * @author Škoda Petr
 */
public interface ExecutionContext {

    /**
     *
     * @return True if component should stop (fail) as soon as possible.
     */
    public boolean canceled();

}
