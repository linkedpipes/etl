package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TaskExecutionTest extends TaskExecution<TaskMock> {

    List<TaskMock> tasks = null;

    TaskExecutionConfiguration configuration = null;

    Component.Context context;

    static Path tempDirectory;

    public TaskExecutionTest() {

        context = new Context() {

            @Override
            public void sendMessage(Event message) {
                // Ignore
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        };

        progressReport =  new ProgressReport() {

            @Override
            public void start(long entriesToProcess) {
                // Ignore
            }

            @Override
            public void start(Collection<?> collection) {
                // Ignore
            }

            @Override
            public void entryProcessed() {
                // Ignore
            }

            @Override
            public void done() {
                // Ignore
            }
        };

    }

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return configuration;
    }

    @Override
    protected List<TaskMock> loadTasks() {
        return tasks;
    }

    @Override
    protected ReportWriter createReportWriter() {
        return new ReportWriter() {

            @Override
            public void onTaskFinished(Task task, Date start, Date end) {
                // No action.
            }

            @Override
            public void onTaskFailed(
                    Task task, Date start, Date end, Throwable throwable) {
                // No action.
            }

            @Override
            public void onTaskFinishedInPreviousRun(Task task) {
                // No action.
            }

            @Override
            public String getIriForReport(Task task) {
                return task.getIri();
            }

        };
    }

    @Override
    protected TaskConsumer<TaskMock> createConsumer() {
        return task -> {
            --task.failCounter;
            if (task.failCounter > 0) {
                throw new LpException("Task failed.");
            }
        };
    }

    @BeforeAll
    public static void initialize() throws IOException {
        tempDirectory = Files.createTempDirectory("lp-etl-");
    }

    @AfterAll
    public static  void cleanup() throws IOException {
        Files.walk(tempDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void execute() throws LpException {
        this.workingDirectory = new WorkingDirectory(tempDirectory.toFile());
        tasks = Arrays.asList(
                new TaskMock("1", "A"),
                new TaskMock("2", "B"));
        configuration = new TaskExecutionConfiguration();
        this.execute(context);
    }

    @Test
    public void executeWithFailures() throws LpException {
        this.workingDirectory = new WorkingDirectory(tempDirectory.toFile());
        tasks = Arrays.asList(
                new TaskMock("1", "A", 1),
                new TaskMock("2", "B", 1));
        configuration = new TaskExecutionConfiguration();
        configuration.numberOfRetries = 2;
        this.execute(context);
    }

}
