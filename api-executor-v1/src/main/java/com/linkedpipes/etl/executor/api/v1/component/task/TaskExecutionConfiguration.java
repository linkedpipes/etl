package com.linkedpipes.etl.executor.api.v1.component.task;

public interface TaskExecutionConfiguration {

    /**
     * @return Number of thread used for execution.
     */
    int getThreadsNumber();

    /**
     * @return True to skip task on error.
     */
    boolean isSkipOnError();

}
