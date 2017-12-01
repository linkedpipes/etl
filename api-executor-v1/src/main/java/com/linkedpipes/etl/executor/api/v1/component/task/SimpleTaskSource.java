package com.linkedpipes.etl.executor.api.v1.component.task;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

class SimpleTaskSource<T extends Task> implements TaskSource<T> {

    private final ConcurrentLinkedDeque<T> tasks;

    private boolean skipOnError = false;

    private volatile boolean taskFailed = false;

    @Override
    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    @Override
    public T getTask() {
        if (shouldHandleNextTask()) {
            return tasks.poll();
        } else {
            return null;
        }
    }

    private boolean shouldHandleNextTask() {
        return skipOnError || !taskFailed;
    }

    @Override
    public void onTaskFinished(T task) {
        // No operation here.
    }

    @Override
    public void onTaskFailed(T task) {
        this.taskFailed = true;
    }

    @Override
    public boolean doesTaskFailed() {
        return this.taskFailed;
    }

    public SimpleTaskSource(Collection<T> tasks) {
        this.tasks = new ConcurrentLinkedDeque<>(tasks);
    }

}
