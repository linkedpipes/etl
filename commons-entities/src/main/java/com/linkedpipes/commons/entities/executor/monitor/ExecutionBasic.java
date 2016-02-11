package com.linkedpipes.commons.entities.executor.monitor;

import java.util.Date;

import com.linkedpipes.commons.entities.rest.Progress;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionBasic {

    private String id;

    private String uri;

    private String pipelineUri;

    private String label;

    private Date start;

    private Date end;

    /**
     * @see cz.cuni.mff.xrg.cuv.commons.entities.vocabulary.ExecutionStatus
     */
    private int statusCode;

    /**
     * Time of last change in pipeline status.
     */
    private Date lastUpdate;

    /**
     * How many components are completed.
     */
    private Progress pipelineProgress;

    /**
     * Name of currently running component.
     */
    private String currentComponentLabel;

    /**
     * Progress of currently running component.
     */
    private Progress currentComponentProgress;

    private boolean running;

    private Long executionDirectorySize;

    public ExecutionBasic() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPipelineUri() {
        return pipelineUri;
    }

    public void setPipelineUri(String pipelineUri) {
        this.pipelineUri = pipelineUri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Progress getPipelineProgress() {
        return pipelineProgress;
    }

    public void setPipelineProgress(Progress pipelineProgress) {
        this.pipelineProgress = pipelineProgress;
    }

    public String getCurrentComponentLabel() {
        return currentComponentLabel;
    }

    public void setCurrentComponentLabel(String currentComponentLabel) {
        this.currentComponentLabel = currentComponentLabel;
    }

    public Progress getCurrentComponentProgress() {
        return currentComponentProgress;
    }

    public void setCurrentComponentProgress(Progress currentComponentProgress) {
        this.currentComponentProgress = currentComponentProgress;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Long getExecutionDirectorySize() {
        return executionDirectorySize;
    }

    public void setExecutionDirectorySize(Long executionDirectorySize) {
        this.executionDirectorySize = executionDirectorySize;
    }

}
