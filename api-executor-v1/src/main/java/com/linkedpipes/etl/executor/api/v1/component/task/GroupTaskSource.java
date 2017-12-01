package com.linkedpipes.etl.executor.api.v1.component.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

class GroupTaskSource<T extends GroupTask> implements TaskSource<T> {

    private class Group<T> {

        private volatile int numberOfRunning;

        protected ConcurrentLinkedDeque<T> tasks = new ConcurrentLinkedDeque<>();

        public void addTask(T task) {
            this.tasks.add(task);
        }

        public void onTaskExecutionFinished() {
            --numberOfRunning;
        }

        public T getTask(int runningLimit) {
            if (numberOfRunning >= runningLimit ) {
                return null;
            }
            T task = tasks.poll();
            if (task == null) {
                return null;
            } else {
                ++numberOfRunning;
                return task;
            }
        }

    }

    private class NullGroup<T> extends Group {

        @Override
        public Object getTask(int runningLimit) {
            return tasks.poll();
        }
    }

    private final Map<Object, Group> groups = new HashMap<>();

    private final int runningLimit;

    private boolean skipOnError = false;

    private volatile boolean taskFailed = false;

    private final Object lock = new Object();

    public GroupTaskSource(Collection<T> tasks, int runningLimit) {
        this.runningLimit = runningLimit;
        this.groups.put(null, new NullGroup<>());
        splitTasks(tasks);
    }

    private void splitTasks(Collection<T> tasks) {
        tasks.forEach((T task) -> {
            Group<T> group = groups.computeIfAbsent(
                    task.getGroup(), (x) -> new Group<>());
            group.addTask(task);
        });

    }

    @Override
    public void setSkipOnError(boolean endOnError) {
        this.skipOnError = endOnError;
    }

    @Override
    public T getTask() {
        if (shouldHandleNextTask()) {
            return getTaskToExecute();
        } else {
            return null;
        }
    }

    private T getTaskToExecute() {
        synchronized (lock) {
            for (Group<T> group : groups.values()) {
                T task = group.getTask(this.runningLimit);
                if (task != null) {
                    return task;
                }
            }
        }
        return null;
    }

    private boolean shouldHandleNextTask() {
        return skipOnError || !taskFailed;
    }

    @Override
    public void onTaskFinished(T task) {
        onTaskExecutionEnd(task);
    }

    private void onTaskExecutionEnd(T task) {
        Group<T> group = groups.get(task.getGroup());
        group.onTaskExecutionFinished();
    }

    @Override
    public void onTaskFailed(T task) {
        onTaskExecutionEnd(task);
        this.taskFailed = true;
    }

    @Override
    public boolean doesTaskFailed() {
        return this.taskFailed;
    }


}
