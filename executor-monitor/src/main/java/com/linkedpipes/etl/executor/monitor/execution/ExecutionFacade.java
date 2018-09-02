package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.executor.Executor;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutionFacade {

    private ExecutionStorage storage;

    @Autowired
    public ExecutionFacade(ExecutionStorage storage) {
        this.storage = storage;
    }

    public Execution getExecution(String id) {
        return storage.getExecution(id);
    }

    public Execution getLivingExecution(String id) {
        Execution execution = storage.getExecution(id);
        if (execution == null) {
            return null;
        }
        if (execution.getStatus() == ExecutionStatus.DELETED) {
            return null;
        } else {
            return execution;
        }
    }

    public Collection<Execution> getExecutions() {
        return storage.getExecutions();
    }

    public Collection<Execution> getExecutions(Date changedSince) {
        return this.storage.getExecutions().stream()
                .filter((exec) -> exec.changedAfter(changedSince))
                .collect(Collectors.toList());
    }

    public Statements getComponentMessages(
            Execution execution, String component) throws IOException {
        MessageLoader messageLoader = new MessageLoader();
        return messageLoader.loadComponentMessages(execution, component);
    }

    public File getExecutionLogFile(Execution execution) {
        return new File(execution.getDirectory(), "log/execution.log");
    }

    public Statements getExecutionStatements(Execution execution)
            throws MonitorException {
        ExecutionLoader executionLoader = new ExecutionLoader();
        return executionLoader.loadStatements(execution);
    }

    public JsonNode getOverview(Execution execution) {
        return execution.getOverview();
    }

    public Execution createExecution(
            Collection<Statement> pipeline, List<MultipartFile> inputs)
            throws MonitorException {
        return this.storage.createExecution(pipeline, inputs);
    }

    public void deleteExecution(Execution execution) {
        this.storage.delete(execution);
    }

    /**
     * Parse execution in given stream and match it with existing execution.
     * If no execution with given IRI is not find then throws an exception.
     *
     * @return Null if no execution was discovered.
     */
    public Execution discoverExecution(InputStream stream)
            throws MonitorException {
        return this.storage.discover(stream);
    }

    /**
     * Update given execution from given stream with execution overview.
     */
    public void update(Execution execution, InputStream stream)
            throws MonitorException {
        this.storage.updateFromOverview(execution, stream);
    }

    /**
     * Must be called when an executor is assigned to the execution.
     *
     * @param execution
     */
    public void onAttachExecutor(Execution execution, Executor executor) {
        if (!ExecutionStatus.isFinished(execution.getStatus())) {
            return;
        }
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setExecutor(executor);
        this.updateMonitorInfo(execution);
    }

    private void updateMonitorInfo(Execution execution) {
        MonitorStatements monitorStatements = new MonitorStatements();
        monitorStatements.update(execution);
    }

    public void onUnresponsiveExecutor(Execution execution) {
        if (ExecutionStatus.isFinished(execution.getStatus())) {
            return;
        }
        execution.setStatus(ExecutionStatus.UNRESPONSIVE);
        this.updateMonitorInfo(execution);
    }

    /**
     * Must be called when an executor is detached from the execution. Ie.
     * if in state UNRESPONSIVE (or without executor) and all known
     * executors ARE EXECUTING other pipelines.
     */
    public void onDetachExecutor(Execution execution) throws MonitorException {
        this.storage.checkExecutionFromDirectory(execution);
        if (ExecutionStatus.isFinished(execution.getStatus())) {
            return;
        }
        execution.setStatus(ExecutionStatus.DANGLING);
        execution.setExecutor(null);
        this.updateMonitorInfo(execution);
    }

}
