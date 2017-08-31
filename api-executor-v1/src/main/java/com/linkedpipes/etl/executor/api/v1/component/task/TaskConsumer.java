package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;

public interface TaskConsumer <T>  {

    void accept(T task) throws LpException;

}
