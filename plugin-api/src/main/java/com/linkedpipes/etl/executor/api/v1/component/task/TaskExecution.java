package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Base class to extend by task-based components.
 */
public abstract class TaskExecution<T extends Task>
        implements Component, SequentialExecution {

    private static final int TERMINATION_TIMEOUT_S = 5;

    private static final String CHECKPOINT_FILE_NAME_PADDING = "000000";

    private static final String CHECKPOINT_DIRECTORY_NAME = "checkpoints";

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecution.class);

    private File checkpointDirectory;

    /**
     * We need this directory to save progress.
     */
    @Component.Inject
    public WorkingDirectory workingDirectory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute(Component.Context context) throws LpException {
        onInitialize(context);
        TaskExecutionConfiguration configuration = getExecutionConfiguration();
        prepareCheckpointDirectory();
        List<T> tasks = loadTasks();
        TaskSource<T> taskSource = new TaskSource<>(
                context, progressReport, createReportWriter(),
                configuration, tasks);
        List<TaskConsumerWrap<T>> executors = createConsumersWraps(
                taskSource, configuration.numberOfThreads);
        onExecutionWillBegin(tasks);
        ExecutorService executorService = createExecutorService(
                configuration.numberOfThreads);
        executeTasks(executorService, executors);
        waitForShutdown(executorService);
        onExecutionDidFinished();
        checkForFailures(configuration, taskSource);
    }

    /**
     * Called after only context is set in execute method.
     */
    protected void onInitialize(Component.Context context) throws LpException {
        // No action here.
    }

    /**
     * Return a task execution configuration.
     */
    protected abstract TaskExecutionConfiguration getExecutionConfiguration();

    private void prepareCheckpointDirectory() {
        checkpointDirectory = new File(
                workingDirectory, CHECKPOINT_DIRECTORY_NAME);
        if (!checkpointDirectory.exists()) {
            if (checkpointDirectory.mkdirs()) {
                LOG.warn("Can't create checkpoint directory.");
            }
        }
    }

    /**
     * Return all tasks to be executed.
     */
    protected abstract List<T> loadTasks() throws LpException;

    protected abstract  ReportWriter createReportWriter() throws LpException;

    private List<TaskConsumerWrap<T>> createConsumersWraps(
            TaskSource<T> taskSource, int count) throws LpException {
        List<TaskConsumerWrap<T>> executors = new ArrayList<>();
        for (int index = 0; index < count; ++index) {
            TaskConsumer<T> consumer = createConsumer();
            File checkpointFile = getTaskCheckpointFile(index);
            TaskConsumerWrap<T> consumerWrap  = new TaskConsumerWrap<>(
                    consumer, taskSource, checkpointFile);
            executors.add(consumerWrap);
        }
        return executors;
    }

    /**
     * This function is called before the workers are created.
     */
    protected void onExecutionWillBegin(List<T> tasks) throws LpException {
        progressReport.start(tasks);
    }

    private ExecutorService createExecutorService(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads);
    }

    /**
     * Return new instance of task consumer.
     */
    protected abstract TaskConsumer<T> createConsumer() throws LpException;

    private File getTaskCheckpointFile(int index) {
        String indexAsStr = Integer.toString(index);
        // Just to make it look nicer.
        if (indexAsStr.length() < CHECKPOINT_FILE_NAME_PADDING.length()) {
            indexAsStr = CHECKPOINT_FILE_NAME_PADDING.substring(
                    indexAsStr.length()) + indexAsStr;
        }
        return new File(checkpointDirectory, indexAsStr);
    }

    private void executeTasks(
            ExecutorService executorService,
            List<TaskConsumerWrap<T>> executors) {
        for (TaskConsumerWrap<T> executor : executors) {
            executorService.submit(executor);
        }
    }

    private void waitForShutdown(ExecutorService executor) {
        // Disable submit of new tasks and wait for the end.
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(
                        TERMINATION_TIMEOUT_S, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
            }
        }
    }

    protected void onExecutionDidFinished() throws LpException {
        progressReport.done();
    }

    private void checkForFailures(
            TaskExecutionConfiguration configuration, TaskSource<T> taskSource)
            throws LpException {
        if (configuration.skipFailedTasks) {
            // Even if there are failures we do not care.
            return;
        }
        if (taskSource.hasTaskExecutionFailed()) {
            throw new LpException("At least one task failed.");
        }
    }

}
