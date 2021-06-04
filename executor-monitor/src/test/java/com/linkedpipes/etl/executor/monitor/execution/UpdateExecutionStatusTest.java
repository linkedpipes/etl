package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

public class UpdateExecutionStatusTest {

    private UpdateExecutionStatus updater = new UpdateExecutionStatus();

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void noStateChange() {
        Execution execution = new Execution();
        execution.setExecutorResponsive(true);
        execution.setExecutor(true);
        execution.setStatus(ExecutionStatus.RUNNING);
        Date lastChange = new Date();
        execution.setLastChange(lastChange);
        execution.setOverviewJson(createOverviewJson());
        OverviewObject overview = Mockito.mock(OverviewObject.class);
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.RUNNING.asStr());
        //
        boolean changed = updater.update(execution, overview);
        Assertions.assertFalse(changed);
        Mockito.verify(overview, Mockito.times(1)).setStatus(
                ExecutionStatus.RUNNING.asStr());
        Assertions.assertEquals(lastChange, execution.getLastChange());
    }

    private ObjectNode createOverviewJson() {
        ObjectNode node = mapper.createObjectNode();
        ObjectNode status = mapper.createObjectNode().put("@id", "");
        node.set("status", status);
        return node;
    }

    @Test
    public void finished() {
        Execution execution = new Execution();
        execution.setStatus(ExecutionStatus.RUNNING);
        OverviewObject overview = Mockito.mock(OverviewObject.class);
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.FINISHED.asStr());
    }

    @Test
    public void cancelled() {
        Execution execution = new Execution();
        execution.setExecutorResponsive(true);
        execution.setExecutor(true);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setOverviewJson(createOverviewJson());
        OverviewObject overview = Mockito.mock(OverviewObject.class);
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.CANCELLING.asStr());
        boolean changed;
        //
        changed = updater.update(execution, overview);
        Assertions.assertTrue(changed);
        Mockito.verify(overview, Mockito.times(1)).setStatus(
                ExecutionStatus.CANCELLING.asStr());
        //As we do not have finish date we ignore change of the state.
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.CANCELLED.asStr());
        changed = updater.update(execution, overview);
        Assertions.assertFalse(changed);
        Mockito.verify(overview, Mockito.times(2)).setStatus(
                ExecutionStatus.CANCELLING.asStr());
        // Switch only once finish time is given.
        Mockito.when(overview.getFinish()).thenReturn(new Date());
        changed = updater.update(execution, overview);
        Assertions.assertTrue(changed);
        Mockito.verify(overview, Mockito.times(1)).setStatus(
                ExecutionStatus.CANCELLED.asStr());
    }

    @Test
    public void unresponsiveExecutor() {
        Execution execution = new Execution();
        execution.setExecutorResponsive(false);
        execution.setExecutor(true);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setOverviewJson(createOverviewJson());
        OverviewObject overview = Mockito.mock(OverviewObject.class);
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.RUNNING.asStr());
        boolean changed = updater.update(execution, overview);
        //
        Assertions.assertTrue(changed);
        Mockito.verify(overview, Mockito.times(1)).setStatus(
                ExecutionStatus.UNRESPONSIVE.asStr());
    }

    @Test
    public void missingExecutor() {
        Execution execution = new Execution();
        execution.setExecutorResponsive(false);
        execution.setExecutor(false);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setOverviewJson(createOverviewJson());
        OverviewObject overview = Mockito.mock(OverviewObject.class);
        Mockito.when(overview.getStatus()).thenReturn(
                ExecutionStatus.RUNNING.asStr());
        boolean changed = updater.update(execution, overview);
        //
        Assertions.assertTrue(changed);
        Mockito.verify(overview, Mockito.times(1)).setStatus(
                ExecutionStatus.DANGLING.asStr());
    }

}
