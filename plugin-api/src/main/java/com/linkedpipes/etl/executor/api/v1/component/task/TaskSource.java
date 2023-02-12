package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TaskSource<T extends Task> {

    /**
     * Represent a group of tasks.
     */
    private static class TaskGroup<T extends Task> {

        /**
         * All tasks for execution.
         */
        public Set<TaskWrap<T>> tasksForExecution = new HashSet<>();

        /**
         * Map of running tasks.
         */
        public Map<String, TaskWrap<T>> runningTasks = new HashMap<>();

        /**
         * Next task from this group can not be executed before given time.
         * We need one value for each thread.
         */
        public Map<Thread, LocalTime> nextExecutionTimes = new HashMap<>();

        public int numberOfFailedTasks = 0;

    }

    private static final int SLEEP_TIME_MS = 100;

    /**
     * Set to true to stop execution as soon as possible, by not
     * giving away any tasks.
     */
    private volatile boolean shouldBeTerminated = false;

    /**
     * Source wide lock.
     */
    private final Object lock = new Object();

    private final Component.Context context;

    private final ProgressReport progressReport;

    private final ReportWriter reportWriter;

    private final TaskExecutionConfiguration configuration;

    private final Map<String, TaskGroup<T>> groups;

    TaskSource(Component.Context context,
               ProgressReport progressReport,
               ReportWriter reportWriter,
               TaskExecutionConfiguration configuration,
               Collection<T> tasks) {
        this.context = context;
        this.progressReport = progressReport;
        this.reportWriter = reportWriter;
        this.configuration = configuration;
        this.groups = splitTasksToGroups(tasks);
    }

    private Map<String, TaskGroup<T>> splitTasksToGroups(Collection<T> tasks) {
        Map<String, TaskGroup<T>> result = new HashMap<>();
        tasks.forEach((T task) -> {
            TaskGroup<T> group = result.computeIfAbsent(
                    task.getGroup(),
                    (x) -> new TaskGroup<>());
            group.tasksForExecution.add(new TaskWrap<>(task));
        });
        return result;
    }

    /**
     * This is blocking call waiting for another task.
     * When null is returned the execution should finish.
     * This call also return null if execution was cancelled.
     */
    public T getTaskOrWait() {
        while (true) {
            if (context.isCancelled() || shouldBeTerminated) {
                return null;
            }
            T result = getTaskWrap();
            if (result != null) {
                return result;
            }
            waitBeforeNextTry();
        }
    }

    /**
     * Used mostly for tests of the source, where we do not want to wait.
     */
    public T getTaskWrap() {
        synchronized (lock) {
            TaskWrap<T> result = getNextTaskForExecution();
            if (result == null) {
                return null;
            }
            return result.task;
        }
    }

    /**
     * Code in this method must be called from one thread at most.
     */
    private TaskWrap<T> getNextTaskForExecution() {
        LocalTime now = LocalTime.now();
        TaskWrap<T> taskForExecution = null;
        // Search for each group.
        boolean areThereRunningOrWaitingTasks = false;
        for (var entry : groups.entrySet()) {
            TaskGroup<T> group = entry.getValue();
            // Check number of running tasks.
            if (group.runningTasks.size()
                    >= configuration.numberOfThreadsPerGroup) {
                areThereRunningOrWaitingTasks = true;
                continue;
            }
            // Check there is something to execute, we check after running
            // to properly set areThereTasksForExecution.
            if (group.tasksForExecution.isEmpty()) {
                continue;
            }
            // There are waiting tasks.
            areThereRunningOrWaitingTasks = true;
            // Check next execution time for the group.
            LocalTime nextExecutionTime =
                    group.nextExecutionTimes.get(Thread.currentThread());
            if (nextExecutionTime != null && nextExecutionTime.isAfter(now)) {
                continue;
            }
            // Search for task to execute.
            for (TaskWrap<T> task : group.tasksForExecution) {
                // Check for next execution time.
                if (task.nextExecutionTime != null
                    && task.nextExecutionTime.isAfter(now)) {
                    continue;
                }
                // We have the task for execution.
                taskForExecution = task;
                break;
            }
            if (taskForExecution == null) {
                continue;
            }
            // Update the group.
            group.runningTasks.put(
                    taskForExecution.task.getIri(), taskForExecution);
            group.tasksForExecution.remove(taskForExecution);
            break;
        }
        if (!areThereRunningOrWaitingTasks) {
            // There is nothing more to execute.
            shouldBeTerminated = true;
        }
        if (taskForExecution == null) {
            return null;
        }
        taskForExecution.executionStart = new Date();
        return taskForExecution;
    }

    /**
     * Put caller thread to sleep.
     */
    private void waitBeforeNextTry() {
        try {
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException ex) {
            // We ignore it here.
        }
    }

    public void onTaskFinished(T task) {
        TaskGroup<T> group = groups.get(task.getGroup());
        TaskWrap<T> wrap = group.runningTasks.get(task.getIri());
        synchronized (lock) {
            updateGroupNextTime(group);
            group.runningTasks.remove(task.getIri());
            // Report progress.
            progressReport.entryProcessed();
        }
        reportWriter.onTaskFinished(
                task, wrap.executionStart, new Date());
    }

    private void updateGroupNextTime(TaskGroup<T> group) {
        if (configuration.waitAfterTaskMs == 0) {
            return;
        }
        LocalTime nextTime = LocalTime.now()
                .plus(configuration.waitAfterTaskMs, ChronoUnit.MILLIS);
        group.nextExecutionTimes.put(Thread.currentThread(), nextTime);
    }

    public void onTaskFailed(T task, Throwable exception) {
        TaskGroup<T> group = groups.get(task.getGroup());
        TaskWrap<T> wrap = group.runningTasks.get(task.getIri());
        synchronized (lock) {
            updateGroupNextTime(group);
            group.runningTasks.remove(task.getIri());
            if (wrap.numerOfFailedAttempts >= configuration.numberOfRetries) {
                // The task has failed, there is no retry.
                ++group.numberOfFailedTasks;
                if (!configuration.skipFailedTasks) {
                    shouldBeTerminated = true;
                }
                // Report progress.
                progressReport.entryProcessed();
                return;
            }
            // We should retry the task.
            ++wrap.numerOfFailedAttempts;
            if (configuration.waitAfterFailedTaskMs > 0) {
                // Set next execution time.
                wrap.nextExecutionTime = LocalTime.now().plus(
                        configuration.waitAfterFailedTaskMs, ChronoUnit.MILLIS);
            }
            // Return it back for execution from running.
            group.tasksForExecution.add(wrap);
        }
        reportWriter.onTaskFailed(
                task, wrap.executionStart, new Date(), exception);
    }

    public boolean hasTaskExecutionFailed() {
        for (TaskGroup<T> group : groups.values()) {
            if (group.numberOfFailedTasks > 0) {
                return true;
            }
        }
        return false;
    }

}
