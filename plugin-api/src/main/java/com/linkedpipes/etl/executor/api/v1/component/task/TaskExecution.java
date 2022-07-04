package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class TaskExecution<T extends Task>
        implements Component, SequentialExecution {

    private static final int TERMINATION_CHECK = 5;

    private static final String CHECKPOINT_FILE_NAME_PADDING = "000000";

    private TaskExecutionConfiguration configuration;

    @Component.Inject
    public WorkingDirectory workingDirectory;

    protected Component.Context context;

    private File checkpointDir;

    private final Set<String> taskFilter = new HashSet<>();

    @Override
    public void execute(Component.Context context) throws LpException {
        this.context = context;
        initialization();
        configuration = getExecutionConfiguration();
        prepareCheckpointDir();
        TaskSource<T> taskSource = createTaskSource();
        taskSource.setSkipOnError(configuration.isSkipOnError());
        List<TaskExecutor<T>> executors = createExecutors(taskSource);
        beforeExecution();
        ExecutorService executorService = createExecutorService();
        start(executorService, executors);
        waitForShutdown(executorService);
        afterExecution();
        checkForFailures(taskSource);
    }

    /**
     * Called after only context is set in execute method.
     */
    protected void initialization() throws LpException {
        // No action here.
    }

    /**
     * This function is called before any other method.
     */
    private void prepareCheckpointDir() throws LpException {
        if (checkpointDir == null) {
            checkpointDir = getCheckpointDir(workingDirectory);
            checkpointDir.mkdirs();
        }
    }

    private static File getCheckpointDir(File workingDirectory) {
        return new File(workingDirectory, "checkpoints");
    }

    protected abstract TaskExecutionConfiguration getExecutionConfiguration();

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(configuration.getThreadsNumber());
    }

    protected abstract TaskSource<T> createTaskSource() throws LpException;

    /**
     * This function is called before the workers are created.
     */
    protected void beforeExecution() throws LpException {
        // No operation here.
    }

    private List<TaskExecutor<T>> createExecutors(TaskSource<T> taskSource)
            throws LpException {
        List<TaskExecutor<T>> executors = new ArrayList<>();
        for (int i = 0; i < configuration.getThreadsNumber(); ++i) {
            executors.add(createTaskExecutor(taskSource, i));
        }
        return executors;
    }

    private TaskExecutor<T> createTaskExecutor(
            TaskSource<T> taskSource, int index) throws LpException {
        return new TaskExecutor<>(
                createConsumer(), taskSource, createReportWriter(),
                context, getTaskCheckpointFile(index), taskFilter);
    }

    protected abstract TaskConsumer<T> createConsumer() throws LpException;

    protected abstract ReportWriter createReportWriter();

    private File getTaskCheckpointFile(int index) {
        String indexAsStr = Integer.toString(index);
        // Just to make it look nicer.
        if (indexAsStr.length() < CHECKPOINT_FILE_NAME_PADDING.length()) {
            indexAsStr = CHECKPOINT_FILE_NAME_PADDING.substring(
                    indexAsStr.length()) + indexAsStr;
        }
        return new File(checkpointDir, indexAsStr);
    }

    protected void start(
            ExecutorService executorService, List<TaskExecutor<T>> executors) {
        for (TaskExecutor<T> executor : executors) {
            executorService.submit(executor);
        }
    }

    private void waitForShutdown(ExecutorService executor) {
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(
                        TERMINATION_CHECK, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
    }

    protected void afterExecution() throws LpException {
        // No operation here.
    }

    protected void checkForFailures(TaskSource<T> taskSource)
            throws LpException {
        if (taskSource.doesTaskFailed() && !configuration.isSkipOnError()) {
            throw new LpException("At least one task failed.");
        }
    }

}
