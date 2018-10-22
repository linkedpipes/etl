package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class ExecutorServiceTest {

    @Test
    public void checkExecutors() {
        ExecutionSource executions = Mockito.mock(ExecutionSource.class);
        ExecutorEventListener eventListener = null;
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getExecutorUri()).thenReturn("iri");
        ExecutorRestClient restClient = null;
        CheckExecutor checker = Mockito.mock(CheckExecutor.class);
        ExecutorService service = new ExecutorService(
                executions, eventListener, configuration, restClient, checker);
        service.onInit();
        //
        Mockito.verify(checker, Mockito.times(1)).check(Mockito.any());
        service.checkExecutors();
        Mockito.verify(checker, Mockito.times(2)).check(Mockito.any());
    }

    @Test
    public void startExecution() throws MonitorException {
        ExecutionSource executions = Mockito.mock(ExecutionSource.class);
        Execution execution = Mockito.mock(Execution.class);
        Mockito.when(execution.getStatus()).thenReturn(ExecutionStatus.QUEUED);
        Mockito.when(executions.getExecutions())
                .thenReturn(Arrays.asList(execution));
        ExecutorEventListener eventListener = Mockito
                .mock(ExecutorEventListener.class);
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getExecutorUri()).thenReturn("iri");
        ExecutorRestClient restClient = Mockito.mock(ExecutorRestClient.class);
        CheckExecutor checker = Mockito.mock(CheckExecutor.class);
        Mockito.doAnswer((call) -> {
            call.getArgumentAt(0, Executor.class).setAlive(true);
            return null;
        }).when(checker).check(Mockito.any());
        ExecutorService service = new ExecutorService(
                executions, eventListener, configuration, restClient, checker);
        service.onInit();
        service.checkExecutors();
        //
        Mockito.verify(restClient, Mockito.times(1))
                .start(Mockito.any(), Mockito.eq(execution));
        Mockito.verify(eventListener, Mockito.times(1))
                .onExecutorHasExecution(Mockito.eq(execution), Mockito.any());
    }

    @Test
    public void cancelExecution() throws MonitorException {
        ExecutionSource executions = Mockito.mock(ExecutionSource.class);
        Execution execution = Mockito.mock(Execution.class);
        Mockito.when(execution.getStatus()).thenReturn(ExecutionStatus.RUNNING);
        Mockito.when(executions.getExecution(Mockito.any(Executor.class)))
                .thenReturn(execution);
        ExecutorEventListener eventListener = null;
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getExecutorUri()).thenReturn("iri");
        ExecutorRestClient restClient = Mockito.mock(ExecutorRestClient.class);
        CheckExecutor checker = Mockito.mock(CheckExecutor.class);
        ExecutorService service = new ExecutorService(
                executions, eventListener, configuration, restClient, checker);
        service.onInit();
        service.cancelExecution(execution, "request");
        //
        Mockito.verify(restClient, Mockito.times(1))
                .cancel(Mockito.any(), Mockito.eq("request"));
    }


}
