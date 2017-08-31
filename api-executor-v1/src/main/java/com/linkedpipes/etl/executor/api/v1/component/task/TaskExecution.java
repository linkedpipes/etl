package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TODO Add support for cancel.
 * TODO Add support for reporting.
 */
public abstract class TaskExecution<T extends Task>
        implements Component, SequentialExecution {

    private TaskExecutionConfiguration configuration;

    @Override
    public void execute() throws LpException {
        this.configuration = getExecutionConfiguration();
        ExecutorService executor = createExecutorService();
        TaskSource<T> taskSource = createTaskSource();
        taskSource.setSkipOnError(this.configuration.isSkipOnError());
        beforeExecution();
        createWorkers(executor, taskSource);
        startWorkers(executor);
        waitForShutdown(executor);
        afterExecution();
        checkForFailures(taskSource);
    }

    protected abstract TaskExecutionConfiguration getExecutionConfiguration();

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(
                this.configuration.getThreadsNumber());
    }

    protected abstract TaskSource<T> createTaskSource() throws LpException;

    protected void beforeExecution() throws LpException {
        // No operation here.
    }

    private void createWorkers(
            ExecutorService executor,
            TaskSource<T> taskSource) {
        for (int i = 0; i < configuration.getThreadsNumber(); ++i) {
            TaskExecutor<T> taskExecutor = createTaskExecutor(taskSource);
            executor.submit(taskExecutor);
        }
    }

    private TaskExecutor<T> createTaskExecutor(TaskSource<T> taskSource) {
        TaskConsumer<T> consumer = createConsumer();
        ReportWriter writer = createReportWriter();
        return new TaskExecutor<>(consumer, taskSource, writer);
    }

    protected abstract TaskConsumer<T> createConsumer();

    protected abstract ReportWriter createReportWriter();

    private void startWorkers(ExecutorService executor) {
        executor.shutdown();
    }

    protected void afterExecution() throws LpException {
        // No operation here.
    }

    private void waitForShutdown(ExecutorService executor) {
        while (true) {
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
    }

    private void checkForFailures(TaskSource<T> taskSource) throws LpException {
        if (taskSource.doesTaskFailed() && !configuration.isSkipOnError()) {
            throw new LpException("At least one task failed.");
        }
    }

}
