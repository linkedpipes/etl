package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Date;
import java.util.Map;

class TaskExecutor<T extends Task> implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final TaskConsumer<T> taskConsumer;

    private final TaskSource<T> taskSource;

    private final ReportWriter reportWriter;

    private final Map<String, String> contextMap =
            MDC.getCopyOfContextMap();

    public TaskExecutor(
            TaskConsumer<T> taskConsumer,
            TaskSource<T> taskSource,
            ReportWriter reportWriter) {
        this.taskConsumer = taskConsumer;
        this.taskSource = taskSource;
        this.reportWriter = reportWriter;
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
        MDC.setContextMap(contextMap);
    }

    private void execute() {
        while (true) {
            LOG.debug("Getting task ...");
            T task = taskSource.getTask();
            if (task == null) {
                LOG.debug("There is no task to execute");
                return;
            } else {
                LOG.debug("Executing task ...");
                executeTask(task);
                LOG.debug("Executing task ... done");
            }
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
    }

    private void onTaskFailed(T task, Date startTime, Throwable throwable) {
        reportWriter.onTaskFailed(task, startTime, new Date(), throwable);
        taskSource.onTaskFailed(task);
    }

}
