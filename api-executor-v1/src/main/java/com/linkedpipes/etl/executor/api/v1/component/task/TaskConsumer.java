package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;

/**
 * A single instance of this interface is not called by more than
 * one thread.
 *
 * @param <T>
 */
public interface TaskConsumer <T>  {

    void accept(T task) throws LpException;

}
