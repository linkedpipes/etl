package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;

/**
 * This class does not have to be thread save as it is never caller
 * by more than one thread.
 */
public interface TaskConsumer<T> {

    /**
     * Handle task processing, i.e. consume the task.
     */
    void accept(T task) throws LpException;

}
