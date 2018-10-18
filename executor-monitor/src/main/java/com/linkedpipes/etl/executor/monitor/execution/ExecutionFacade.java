package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutionFacade {

    private ExecutionStorage storage;

    private final MessagesLoader messageLoader = new MessagesLoader();

    private final ExecutionLoader executionLoader = new ExecutionLoader();

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

    public File getExecutionDebugLogFile(Execution execution) {
        return new File(execution.getDirectory(), "log/execution-debug.log");
    }

    public File getExecutionInfoLogFile(Execution execution) {
        return new File(execution.getDirectory(), "log/execution-warn.log");
    }

    public JsonNode getOverview(Execution execution) {
        return execution.getOverviewJson();
    }

    public Execution createExecution(
            Collection<Statement> pipeline, List<MultipartFile> inputs)
            throws MonitorException {
        return this.storage.createExecution(pipeline, inputs);
    }

    public void deleteExecution(Execution execution) {
        this.storage.delete(execution);
    }

    public Statements getExecutionStatements(Execution execution)
            throws MonitorException {
        Statements statements = this.executionLoader.loadStatements(execution);
        // We use the date obtained to update, in this way we can be sure,
        // that we have the latest data.
        if (!statements.isEmpty() && !execution.isHasFinalData()) {
            this.storage.updateFromExecution(execution, statements);
        }
        return statements;
    }

    public Statements getMessages(Execution execution, String component)
            throws IOException {
        return this.messageLoader.loadComponentMessages(execution, component);
    }

}
