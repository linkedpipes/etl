package com.linkedpipes.etl.executor.api.v1.component.task;

public interface TaskExecutionConfiguration {

    /**
     * Number of thread used for execution.
     */
    int getThreadsNumber();

    /**
     * True to skip task on error.
     */
    boolean isSkipOnError();

}
