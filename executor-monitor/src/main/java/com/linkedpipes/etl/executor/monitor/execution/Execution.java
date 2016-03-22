package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.openrdf.model.Statement;

/**
 * Represents a single execution.
 *
 * @author Petr Škoda
 */
public class Execution {

    public enum StatusType {
        /**
         * Queued for an execution.
         *//**
         * Queued for an execution.
         */
        QUEUED,
        /**
         * Running with executor.
         */
        RUNNING,
        /**
         * Finished.
         */
        FINISHED,
        /**
         * Pipeline detected from directory and has running state, but
         * no executor was found.
         */
        DANGLING,
        /**
         * Pipeline was running with executor but the executor is
         * unresponsive.
         */
        UNRESPONSIVE,
        /**
         * Deleted execution, represents a tombstone. When status is set
         * to this value the {@link #lastCheck} must also be updated as
         * it's used to determine time of tombstone removal.
         */
        DELETED
    }

    private String iri;

    /**
     * Path to the execution directory.
     */
    private File directory;

    /**
     * Time of last update (reload, check) of this record.
     */
    private Date lastCheck;

    /**
     * Time of last detected change on the execution;
     */
    private Date lastChange;

    /**
     * If {@link #status} is set to {@link StatusType#DELETED} then
     * determine time of removal.
     */
    private Date timeToLive;

    /**
     * Statements for the execution list extracted from and execution graph.
     */
    private List<Statement> executionStatements = Collections.EMPTY_LIST;

    /**
     * Statements for the execution list extracted from the pipeline graph.
     */
    private List<Statement> pipelineStatements = Collections.EMPTY_LIST;

    /**
     * Internal execution status.
     */
    private StatusType status;

    /**
     * Store informations about execution debug data.
     */
    private DebugData debugData;

    Execution() {

    }

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

    public List<Statement> getExecutionStatements() {
        return Collections.unmodifiableList(executionStatements);
    }

    void setExecutionStatements(List<Statement> executionStatements) {
        this.executionStatements = executionStatements;
    }

    public List<Statement> getPipelineStatements() {
        return Collections.unmodifiableList(pipelineStatements);
    }

    void setPipelineStatements(List<Statement> pipelineStatements) {
        this.pipelineStatements = pipelineStatements;
    }

    StatusType getStatus() {
        return status;
    }

    void setStatus(StatusType status) {
        this.status = status;
    }

    public DebugData getDebugData() {
        return debugData;
    }

    void setDebugData(DebugData debugData) {
        this.debugData = debugData;
    }

}
