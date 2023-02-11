package com.linkedpipes.etl.executor.api.v1.component.task;

/**
 * Configuration object for task execution.
 */
public class TaskExecutionConfiguration {

    /**
     * Number of threads to employ, this also determine the maximum number of
     * concurrently running tasks.
     */
    public int numberOfThreads = 1;

    /**
     * Limit maximum number of tasks running in a group. Should there
     * be no limit, set this value to number of threads.
     */
    public int numberOfThreadsPerGroup = 1;

    /**
     * It true failed tasks do not halt the execution.
     */
    public boolean skipFailedTasks = false;

    /**
     * How many times should a task be retries before it is considered
     * to failed.
     */
    public int numberOfRetries = 0;

    /**
     * How long to wait, in a group, after execution of a task,
     * before executing another. This time is applied for successful and
     * failed tasks alike.
     *
     * If maximumNumberOfRunningTasksInGroup is greater than one,
     * the behavior is not defined.
     */
    public long waitAfterTaskMs = 0;

    /**
     * How long before task can be retried in a group. This applies for
     * a specific task.
     */
    public long waitAfterFailedTaskMs = 0;

}
