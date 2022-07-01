package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.WorkingDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO Add test for failing task.
 */
public class TaskExecutionTest {

    private static class MockedTask implements Task {

        @Override
        public String getIri() {
            return "http://task";
        }
    }

    private static class MockedTaskExecution extends TaskExecution<Task> {

        TaskExecutionConfiguration config =
                Mockito.mock(TaskExecutionConfiguration.class);

        List<Task> tasks = new ArrayList<>();

        List<TaskConsumer> consumers = new ArrayList<>();

        ReportWriter report = Mockito.mock(ReportWriter.class);

        @Override
        protected TaskExecutionConfiguration getExecutionConfiguration() {
            return this.config;
        }

        @Override
        protected TaskSource<Task> createTaskSource() {
            return TaskSource.defaultTaskSource(tasks);
        }

        @Override
        protected TaskConsumer<Task> createConsumer() {
            TaskConsumer consumer = Mockito.mock(TaskConsumer.class);
            consumers.add(consumer);
            return consumer;
        }

        @Override
        protected ReportWriter createReportWriter() {
            return report;
        }
    }

    private File workingDirectory;

    @BeforeAll
    public void before() throws IOException {
        MDC.setContextMap(Collections.EMPTY_MAP);
        workingDirectory = Files.createTempDirectory("lp-test-").toFile();
    }

    @AfterAll
    public void after() {
        deleteDir(workingDirectory);
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    @Test
    public void executeOneConsumer() throws LpException {
        MockedTaskExecution component = new MockedTaskExecution();
        Mockito.when(component.config.getThreadsNumber()).thenReturn(1);
        Mockito.when(component.config.isSkipOnError()).thenReturn(false);
        component.tasks.add(new MockedTask());
        component.tasks.add(new MockedTask());

        component.workingDirectory = new WorkingDirectory(
                new File(workingDirectory, "executeOneConsumer"));

        Component.Context context = Mockito.mock(Component.Context.class);
        component.execute(context);

        Assertions.assertEquals(1, component.consumers.size());
        TaskConsumer<Task> consumer = component.consumers.get(0);
        Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.any());
        Mockito.verify(component.report, Mockito.times(2)).onTaskFinished(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeTwoConsumers() throws LpException {
        MockedTaskExecution component = new MockedTaskExecution();
        Mockito.when(component.config.getThreadsNumber()).thenReturn(2);
        Mockito.when(component.config.isSkipOnError()).thenReturn(false);
        component.tasks.add(new MockedTask());
        component.tasks.add(new MockedTask());

        component.workingDirectory = new WorkingDirectory(
                new File(workingDirectory, "executeTwoConsumers"));

        Component.Context context = Mockito.mock(Component.Context.class);
        component.execute(context);

        Assertions.assertEquals(2, component.consumers.size());
        Mockito.verify(component.report, Mockito.times(2)).onTaskFinished(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

}
