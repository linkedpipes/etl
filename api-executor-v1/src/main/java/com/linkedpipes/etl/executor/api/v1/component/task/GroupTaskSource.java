package com.linkedpipes.etl.executor.api.v1.component.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

class GroupTaskSource<T extends GroupTask> implements TaskSource<T> {

    /**
     * Group of tasks, limit the number of running tasks.
     */
    private class Group<G> {

        private volatile AtomicInteger numberOfRunning = new AtomicInteger();

        protected ConcurrentLinkedDeque<G> tasks =
                new ConcurrentLinkedDeque<>();

        private void addTask(G task) {
            this.tasks.add(task);
        }

        private void onTaskExecutionFinished() {
            numberOfRunning.decrementAndGet();
        }

        protected G getTask(int runningLimit) {
            if (numberOfRunning.get() >= runningLimit) {
                return null;
            }
            G task = tasks.poll();
            if (task == null) {
                return null;
            } else {
                numberOfRunning.incrementAndGet();
                return task;
            }
        }

    }

    /**
     * Group for all tasks without the group, i.e. where group is null.
     */
    private class NullGroup<G> extends Group<G> {

        @Override
        public G getTask(int runningLimit) {
            return tasks.poll();
        }

    }

    private final Map<Object, Group<T>> groups = new HashMap<>();

    private final int runningLimit;

    private boolean skipOnError = false;

    private volatile boolean taskFailed = false;

    private final Object lock = new Object();

    GroupTaskSource(Collection<T> tasks, int runningLimit) {
        this.runningLimit = runningLimit;
        this.groups.put(null, new NullGroup<>());
        splitTasks(tasks);
    }

    private void splitTasks(Collection<T> tasks) {
        tasks.forEach((T task) -> {
            Group<T> group = groups.computeIfAbsent(
                    task.getGroup(),
                    (x) -> new Group<T>());
            group.addTask(task);
        });
    }

    @Override
    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    @Override
    public T getTask() {
        if (shouldHandleNextTask()) {
            return getTaskToExecute();
        } else {
            return null;
        }
    }

    private boolean shouldHandleNextTask() {
        return skipOnError || !taskFailed;
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

    @Override
    public boolean isAllExecuted() {
        if (!shouldHandleNextTask()) {
            return true;
        }
        synchronized (lock) {
            for (Group<T> group : groups.values()) {
                if (!group.tasks.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
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
