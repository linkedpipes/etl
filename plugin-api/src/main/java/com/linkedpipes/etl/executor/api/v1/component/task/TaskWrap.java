package com.linkedpipes.etl.executor.api.v1.component.task;

import java.time.LocalTime;
import java.util.Date;

/**
 * Allow for convenient management of tasks.
 */
class TaskWrap<T extends Task> {

    public T task;

    public int numerOfFailedAttempts = 0;

    /**
     * When set task cannot be executed before given time.
     */
    public LocalTime nextExecutionTime = null;

    public Date executionStart = null;

    public TaskWrap(T task) {
        this.task = task;
    }

}
