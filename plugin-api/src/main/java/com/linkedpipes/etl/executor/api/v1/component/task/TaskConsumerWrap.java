package com.linkedpipes.etl.executor.api.v1.component.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * This class wraps the {@link TaskConsumer}. It should be part of a pool
 * of workers/consumers running in separate threads.
 */
class TaskConsumerWrap<T extends Task> implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskConsumerWrap.class);

    /**
     * We grab compy of the context at the time the thread is created.
     */
    private final Map<String, String> mdc = MDC.getCopyOfContextMap();

    private final TaskConsumer<T> taskConsumer;

    private final TaskSource<T> taskSource;

    /**
     * Finished task are written into this file.
     */
    private final Path checkpointPath;

    TaskConsumerWrap(
            TaskConsumer<T> taskConsumer,
            TaskSource<T> taskSource,
            File checkpointFile) {
        this.taskConsumer = taskConsumer;
        this.taskSource = taskSource;
        this.checkpointPath = checkpointFile.toPath();
    }

    @Override
    public void run() {
        initializeLoggingContext();
        try {
            executeTasks();
            LOG.debug("Task executor is finished.");
        } catch (Throwable throwable) {
            // This should not happen, we keep it here just to be sure.
            LOG.error("Task executor failed with Throwable!", throwable);
        }
    }

    /**
     * Since thread specific this must be called from the worker thread.
     */
    private void initializeLoggingContext() {
        MDC.setContextMap(mdc);
    }

    private void executeTasks() {
        while(true) {
            T task = taskSource.getTaskOrWait();
            if (task == null) {
                return;
            }
            executeTask(task);
        }
    }

    private void executeTask(T task) {
        try {
            LOG.debug("Starting task '{}'", task.getIri());
            taskConsumer.accept(task);
            taskSource.onTaskFinished(task);
            writeFinishedTaskToCheckpoint(task);
        } catch (Throwable throwable) {
            taskSource.onTaskFailed(task, throwable);
        }
    }

    /**
     * Append line with task identification at the end of the file.
     */
    private void writeFinishedTaskToCheckpoint(T task) {
        try {
            String content = String.format("%s%n", task.getIri());
            StandardOpenOption mode = Files.exists(checkpointPath)
                    ? StandardOpenOption.APPEND :
                    StandardOpenOption.CREATE;
            Files.writeString(checkpointPath, content,mode);
        } catch (Throwable ex) {
            LOG.warn("Can't update checkpoint file.", ex);
        }
    }

}
