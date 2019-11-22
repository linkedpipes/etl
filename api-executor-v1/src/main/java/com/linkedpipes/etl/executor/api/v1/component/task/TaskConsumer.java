package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;

/**
 * A single instance of this interface is not called by more than
 * one thread. Therefore the code does not have to be thread save.
 */
public interface TaskConsumer<T> {

    default void setContext(Component.Context context) {
        // Do nothing.
    }

    void accept(T task) throws LpException;

}
