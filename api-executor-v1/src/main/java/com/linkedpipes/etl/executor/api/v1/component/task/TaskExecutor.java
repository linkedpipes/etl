package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;
import java.util.Set;

class TaskExecutor<T extends Task> implements Runnable {

    private static final int SLEEP_TIME = 15 * 1000;

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final TaskConsumer<T> taskConsumer;

    private final TaskSource<T> taskSource;

    private final ReportWriter reportWriter;

    private final Map<String, String> mdcContextMap =
            MDC.getCopyOfContextMap();

    private final Component.Context context;

    /**
     * If provided is used to save finished tasks.
     */
    private final File checkpointFile;

    private final Set<String> taskFilter;

    TaskExecutor(
            TaskConsumer<T> taskConsumer,
            TaskSource<T> taskSource,
            ReportWriter reportWriter,
            Component.Context context,
            File checkpointFile,
            Set<String> taskFilter) {
        this.taskConsumer = taskConsumer;
        this.taskSource = taskSource;
        this.reportWriter = reportWriter;
        this.context = context;
        this.checkpointFile = checkpointFile;
        this.taskFilter = taskFilter;
    }

    @Override
    public void run() {
        initializeLoggingContext();
        try {
            execute();
        } catch (Throwable throwable) {
            LOG.error("Task executor failed with Throwable!", throwable);
        }
    }

    private void initializeLoggingContext() {
        MDC.setContextMap(mdcContextMap);
    }

    private void execute() {
        taskConsumer.setContext(context);
        while (!taskSource.isAllExecuted()) {
            if (context.isCancelled()) {
                break;
            }
            T task = taskSource.getTask();
            if (task == null) {
                // There is no task now, but there might be later.
                waitForNextTask();
                continue;
            }
            if (taskFilter.contains(task.getIri())) {
                reportWriter.onTaskFinishedInPreviousRun(task);
                taskSource.onTaskFinished(task);
            } else {
                executeTask(task);
            }
        }
    }

    private void waitForNextTask() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ex) {
            // Do nothing..
        }
    }

    private void executeTask(T task) {
        Date taskStart = new Date();
        try {
            taskConsumer.accept(task);
            onTaskFinished(task, taskStart);
        } catch (Throwable throwable) {
            onTaskFailed(task, taskStart, throwable);
        }
    }

    private void onTaskFinished(T task, Date startTime) {
        reportWriter.onTaskFinished(task, startTime, new Date());
        taskSource.onTaskFinished(task);
        writeTaskStatus(task);
    }

    private void writeTaskStatus(T task) {
        if (checkpointFile == null) {
            return;
        }
        try {
            String content = String.format("%s%n", task.getIri());
            Files.write(
                    checkpointFile.toPath(),
                    content.getBytes("utf-8"),
                    Files.exists(checkpointFile.toPath())
                            ? StandardOpenOption.APPEND :
                            StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOG.warn("Can't save progress.", ex);
        }
    }

    private void onTaskFailed(T task, Date startTime, Throwable throwable) {
        reportWriter.onTaskFailed(task, startTime, new Date(), throwable);
        taskSource.onTaskFailed(task);
    }

}
