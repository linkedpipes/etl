package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.executor.Executor;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class Execution {

    private String iri;

    private File directory;

    /**
     * Time of last load (reload, check) of this record.
     */
    private Date lastCheck = new Date();

    /**
     * Time of last change as provided by the executor.
     */
    private Date lastExecutionChange;

    /**
     * Time of last update of this execution record. Used to check
     * whether an execution changed since a given time.
     */
    private Date lastChange = new Date();

    /**
     * If {@link #status} is set to {@link ExecutionStatus#DELETED} then
     * determine time of removal.
     */
    private Date timeToLive;

    /**
     * Statements for the execution list extracted from and execution graph.
     */
    private Collection<Statement> overviewStatements = Collections.emptyList();

    /**
     * Overview in form of json-ld with custom format.
     */
    private JsonNode overview;

    /**
     * Selected statements about the pipeline, like label, keywords etc ...
     */
    private Collection<Statement> pipelineStatements = Collections.emptyList();

    private Collection<Statement> monitorStatements = Collections.emptyList();

    private ExecutionStatus status;

    /**
     * Store information about execution debug data.
     */
    private DebugData debugData;

    /**
     * Executor that is executing this execution.
     */
    private Executor executor;

    /**
     * Pipeline resource.
     */
    private Resource pipeline;

    public String getId() {
        return iri.substring(iri.lastIndexOf("/") + 1);
    }

    public String getIri() {
        return iri;
    }

    void setIri(String iri) {
        this.iri = iri;
    }

    public File getDirectory() {
        return directory;
    }

    void setDirectory(File directory) {
        this.directory = directory;
    }

    Date getLastCheck() {
        return lastCheck;
    }

    void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    Date getLastExecutionChange() {
        return lastExecutionChange;
    }

    void setLastExecutionChange(Date lastExecutionChange) {
        this.lastExecutionChange = lastExecutionChange;
        if (lastChange.before(lastExecutionChange)) {
            this.lastChange = lastExecutionChange;
        }
    }

    Date getTimeToLive() {
        return timeToLive;
    }

    void setTimeToLive(Date timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Collection<Statement> getPipelineStatements() {
        return pipelineStatements;
    }

    void setPipelineStatements(Collection<Statement> pipelineStatements) {
        this.pipelineStatements = pipelineStatements;
    }

    public Collection<Statement> getOverviewStatements() {
        return Collections.unmodifiableCollection(overviewStatements);
    }

    void setOverviewStatements(Collection<Statement> overviewStatements) {
        this.overviewStatements = overviewStatements;
    }

    JsonNode getOverview() {
        return overview;
    }

    void setOverview(JsonNode overview) {
        this.overview = overview;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    void setStatus(ExecutionStatus status) {
        if (this.status != status) {
            this.lastChange = new Date();
        }
        this.status = status;
    }

    public DebugData getDebugData() {
        return debugData;
    }

    void setDebugData(DebugData debugData) {
        this.debugData = debugData;
    }

    public Executor getExecutor() {
        return executor;
    }

    void setExecutor(Executor executor) {
        this.executor = executor;
    }

    boolean hasExecutor() {
        return this.executor != null;
    }

    Resource getPipeline() {
        return pipeline;
    }

    void setPipeline(Resource pipeline) {
        this.pipeline = pipeline;
    }

    public Collection<Statement> getMonitorStatements() {
        return monitorStatements;
    }

    void setMonitorStatements(Collection<Statement> monitorStatements) {
        this.monitorStatements = monitorStatements;
    }

    /**
     * @return True if there were changes to this execution since given time.
     */
    boolean changedAfter(Date date) {
        return date.before(this.lastChange);
    }

    /**
     * @return Name of graph used to store data in execution list.
     */
    String getListGraph() {
        return this.iri + "/list";
    }

}
