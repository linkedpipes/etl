package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.debug.DebugDataSource;
import com.linkedpipes.etl.executor.monitor.events.EventListener;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutionFacade implements DebugDataSource {

    private ExecutionStorage storage = null;

    private EventListener eventListener = null;

    private final MessagesLoader messageLoader = new MessagesLoader();

    private final ExecutionLoader executionLoader = new ExecutionLoader();

    @Autowired
    private void setExecutionStorage(ExecutionStorage storage) {
        this.storage = storage;
    }

    @Autowired
    private void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @PostConstruct
    public void onInit() throws MonitorException {
        storage.initialize();
        eventListener.onExecutionFacadeReady(this);
    }

    public Execution getExecutionById(String id) {
        return storage.getExecutionById(id);
    }

    public Execution getLivingExecutionByIri(String iri) {
        Execution execution = storage.getExecutionByIri(iri);
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

    /**
     * For backwards compatibility we also try the older execution file.
     * This is of 2022.06.
     */
    public File getExecutionLogFile(Execution execution) {
        File primary = new File(
                execution.getDirectory(), "log/execution.log");
        if (primary.exists()) {
            return primary;
        }
        File secondary = new File(
                execution.getDirectory(), "log/execution-debug.log");
        if (secondary.exists()) {
            return secondary;
        }
        return primary;
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
            this.storage.updateExecutionDebugData(execution, statements);
        }
        return statements;
    }

    public Statements getMessages(Execution execution, String component)
            throws IOException {
        return this.messageLoader.loadComponentMessages(execution, component);
    }

    @Override
    public DebugData getDebugData(String id) {
        Execution execution = getExecutionById(id);
        if (execution == null) {
            return null;
        }
        return execution.getDebugData();
    }

    public Execution cloneAsNewExecution(Execution execution)
            throws MonitorException {
        return storage.cloneAsNewExecution(execution);
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 1000)
    public void updateExecutions() {
        storage.updateExecutions();
    }

}
