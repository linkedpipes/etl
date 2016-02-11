package com.linkedpipes.executor.monitor.execution.entity;

import com.linkedpipes.commons.entities.executor.Labels;
import java.io.File;
import java.util.Date;

/**
 * A simple record used to monitor execution directories.
 *
 * @author Škoda Petr
 */
public class ExecutionMetadata {

    /**
     * Represent status of the execution.
     */
    public enum Status {

        QUEUED,
        RUNNING,
        DONE
    };

    /**
     * Execution identifier.
     */
    private String id;

    /**
     * Last change as stored in the execution file.
     */
    private transient Date lastChange;

    /**
     * Time of last change on this instance.
     */
    private transient Date updateTime;

    /**
     * Address of the assigned executor.
     */
    private String executorAddress;

    /**
     * Status of the execution.
     */
    private Status status = Status.QUEUED;

    /**
     * Execution root directory, ie. directory with this file.
     */
    private transient File directory;

    /**
     * Pipeline labels, are stored in separated file.
     */
    private transient Labels.Resource labels = new Labels.Resource();

    /**
     * URI of pipeline.
     */
    private String pipelineUri;

    /**
     * Path to the definition file.
     */
    private File definitionFile;

    public ExecutionMetadata() {
    }

    public ExecutionMetadata(String id, File directory, File definitionFile) {
        this.id = id;
        this.directory = directory;
        this.definitionFile = definitionFile;
        this.updateTime = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getExecutorAddress() {
        return executorAddress;
    }

    public void setExecutorAddress(String executorAddress) {
        this.executorAddress = executorAddress;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Labels.Resource getLabel() {
        return labels;
    }

    public void setLabel(Labels.Resource label) {
        this.labels = label;
    }

    public String getPipelineUri() {
        return pipelineUri;
    }

    public void setPipelineUri(String pipelineUri) {
        this.pipelineUri = pipelineUri;
    }

    public File getDefinitionFile() {
        return definitionFile;
    }

    public void setDefinitionFile(File definitionFile) {
        this.definitionFile = definitionFile;
    }

}
