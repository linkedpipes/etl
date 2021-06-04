package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CheckExecutorTest {

    @Test
    public void executorRemainsOffline() {
        ExecutorEventListener listener = null;
        ExecutorRestClient client = Mockito.mock(ExecutorRestClient.class);
        Executor executor = new Executor(null);
        executor.setAlive(false);
        Mockito.when(client.check(executor)).thenThrow(new RuntimeException());
        (new CheckExecutor(listener, client, null)).check(executor);
        //
        Assertions.assertFalse(executor.isAlive());
    }

    @Test
    public void executorBecomesOffline() {
        ExecutorEventListener listener =
                Mockito.mock(ExecutorEventListener.class);
        ExecutorRestClient client = Mockito.mock(ExecutorRestClient.class);
        Executor executor = new Executor(null);
        executor.setAlive(true);
        Mockito.when(client.check(executor)).thenThrow(new RuntimeException());
        (new CheckExecutor(listener, client, null)).check(executor);
        //
        Assertions.assertFalse(executor.isAlive());
        Mockito.verify(listener, Mockito.times(1))
                .onExecutorUnavailable(executor);
    }

    @Test
    public void executorWithoutExecution() {
        ExecutorEventListener listener =
                Mockito.mock(ExecutorEventListener.class);
        ExecutorRestClient client = Mockito.mock(ExecutorRestClient.class);
        Executor executor = new Executor(null);
        executor.setAlive(true);
        Mockito.when(client.check(executor)).thenReturn(null);
        (new CheckExecutor(listener, client, null)).check(executor);
        //
        Assertions.assertTrue(executor.isAlive());
        Mockito.verify(listener, Mockito.times(1))
                .onExecutorWithoutExecution(executor);
    }

    @Test
    public void discoveryExecution() throws MonitorException {
        ExecutorRestClient client = Mockito.mock(ExecutorRestClient.class);
        ExecutionSource executions = Mockito.mock(ExecutionSource.class);
        Executor executor = new Executor(null);
        executor.setAlive(true);
        Mockito.when(client.check(executor)).thenReturn("{}");
        Execution execution = new Execution();
        Mockito.when(executions.getExecution(Mockito.any(JsonNode.class)))
                .thenReturn(execution);
        ExecutorEventListener listener =
                Mockito.mock(ExecutorEventListener.class);
        (new CheckExecutor(listener, client, executions)).check(executor);
        //
        Mockito.verify(listener, Mockito.times(1))
                .onExecutorHasExecution(execution, executor);
        Mockito.verify(listener, Mockito.times(1))
                .onOverview(Mockito.eq(execution), Mockito.any(JsonNode.class));
    }

    @Test
    public void updateFromOverview() {
        ExecutorRestClient client = Mockito.mock(ExecutorRestClient.class);
        ExecutionSource executions = Mockito.mock(ExecutionSource.class);
        Executor executor = new Executor(null);
        executor.setAlive(true);
        Mockito.when(client.check(executor)).thenReturn("{}");
        Execution execution = new Execution();
        Mockito.when(executions.getExecution(executor)).thenReturn(execution);
        ExecutorEventListener listener =
                Mockito.mock(ExecutorEventListener.class);
        (new CheckExecutor(listener, client, executions)).check(executor);
        //
        Mockito.verify(listener, Mockito.times(1))
                .onExecutorHasExecution(execution, executor);
        Mockito.verify(listener, Mockito.times(1))
                .onOverview(Mockito.eq(execution), Mockito.any(JsonNode.class));
    }

}
