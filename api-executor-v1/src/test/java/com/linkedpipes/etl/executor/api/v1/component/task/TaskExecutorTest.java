package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Collections;

public class TaskExecutorTest {

    private static class MockedTask implements Task {

        @Override
        public String getIri() {
            return "http://task";
        }
    }

    private TaskConsumer<Task> consumer;

    private ReportWriter report;

    private Component.Context context;

    @BeforeClass
    public static void initializeMdcContext() {
        MDC.setContextMap(Collections.EMPTY_MAP);
    }

    @Before
    public void before() {
        consumer = Mockito.mock(TaskConsumer.class);
        report = Mockito.mock(ReportWriter.class);
        context = Mockito.mock(Component.Context.class);
        Mockito.when(context.isCancelled()).thenReturn(false);
    }

    @Test
    public void executeTask() {
        MockedTask first = new MockedTask();
        TaskSource<Task> source = TaskSource.defaultTaskSource(
                Arrays.asList(first));
        TaskExecutor<Task> executor = new TaskExecutor<>(
                consumer, source, report, context, null,
                Collections.emptySet());
        executor.run();

        Mockito.verify(report, Mockito.times(1)).onTaskFinished(
                Mockito.eq(first), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeFailTask() throws LpException {
        MockedTask first = new MockedTask();
        TaskSource<Task> source = TaskSource.defaultTaskSource(
                Arrays.asList(first));
        TaskExecutor<Task> executor = new TaskExecutor<>(
                consumer, source, report, context, null,
                Collections.emptySet());
        Mockito.doThrow(LpException.class).when(consumer)
                .accept(Mockito.any());
        executor.run();

        Mockito.verify(report, Mockito.times(1)).onTaskFailed(
                Mockito.eq(first), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeFailTaskAndSkip() throws LpException {
        MockedTask first = new MockedTask();
        MockedTask second = new MockedTask();
        TaskSource<Task> source = TaskSource.defaultTaskSource(
                Arrays.asList(first, second));
        source.setSkipOnError(true);
        TaskExecutor<Task> executor = new TaskExecutor<>(
                consumer, source, report, context, null,
                Collections.emptySet());
        Mockito.doThrow(LpException.class).when(consumer)
                .accept(Mockito.eq(first));
        executor.run();

        Mockito.verify(report, Mockito.times(1)).onTaskFailed(
                Mockito.eq(first), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(report, Mockito.times(1)).onTaskFinished(
                Mockito.eq(second), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeFailTaskAndNotSkip() throws LpException {
        MockedTask first = new MockedTask();
        MockedTask second = new MockedTask();
        TaskSource<Task> source = TaskSource.defaultTaskSource(
                Arrays.asList(first, second));
        source.setSkipOnError(false);
        TaskExecutor<Task> executor = new TaskExecutor<>(
                consumer, source, report, context, null,
                Collections.emptySet());
        Mockito.doThrow(LpException.class).when(consumer)
                .accept(Mockito.eq(first));
        executor.run();

        // Only one the first task fail the second task is not executed.
        Mockito.verify(report, Mockito.times(1)).onTaskFailed(
                Mockito.eq(first), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(report, Mockito.times(1)).onTaskFailed(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(report, Mockito.times(0)).onTaskFinished(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

}
