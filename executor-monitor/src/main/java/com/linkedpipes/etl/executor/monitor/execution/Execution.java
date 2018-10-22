package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class Execution {

    private String iri;

    private File directory;

    private JsonNode overviewJson;

    /**
     * Last change as set in overview.
     */
    private Date lastOverviewChange;

    /**
     * Time of last change of this execution for external API.
     */
    private Date lastChange = new Date();

    /**
     * If {@link #status} is set to {@link ExecutionStatus#DELETED} then
     * determine time of removal.
     */
    private Date timeToLive;

    private Collection<Statement> overviewStatements = Collections.emptyList();

    private Collection<Statement> pipelineStatements = Collections.emptyList();

    private ExecutionStatus status;

    private DebugData debugData;

    /**
     * Has assigned executor.
     */
    private boolean executor;

    /**
     * The executor is responsive.
     */
    private boolean executorResponsive;

    private Resource pipeline;

    /**
     * When execution is updated from a stream, we update only some information.
     * Still the pipeline status can be updated to finished by that check.
     * This can result in a situation where pipeline is finished and not
     * all the information is loaded, for that reason we use this property
     * to force one more reload although the pipeline is finished.
     */
    private boolean hasFinalData = false;

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

    Date getLastChange() {
        return lastChange;
    }

    void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
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

    JsonNode getOverviewJson() {
        return overviewJson;
    }

    void setOverviewJson(JsonNode overviewJson) {
        this.overviewJson = overviewJson;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public DebugData getDebugData() {
        return debugData;
    }

    void setDebugData(DebugData debugData) {
        this.debugData = debugData;
    }

    boolean isExecutor() {
        return executor;
    }

    void setExecutor(boolean executor) {
        this.executor = executor;
    }

    boolean isExecutorResponsive() {
        return executorResponsive;
    }

    void setExecutorResponsive(boolean executorResponsive) {
        this.executorResponsive = executorResponsive;
    }

    public Resource getPipeline() {
        return pipeline;
    }

    void setPipeline(Resource pipeline) {
        this.pipeline = pipeline;
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
    public String getListGraph() {
        return this.iri + "/list";
    }

    public boolean isHasFinalData() {
        return hasFinalData;
    }

    void setHasFinalData(boolean hasFinalData) {
        this.hasFinalData = hasFinalData;
    }

    public Date getLastOverviewChange() {
        return lastOverviewChange;
    }

    void setLastOverviewChange(Date lastOverviewChange) {
        this.lastOverviewChange = lastOverviewChange;
    }

}
