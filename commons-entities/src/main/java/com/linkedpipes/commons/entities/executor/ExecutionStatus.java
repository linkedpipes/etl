package com.linkedpipes.commons.entities.executor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.linkedpipes.commons.entities.rest.Progress;

/**
 * Holds information about execution.
 *
 * @author Škoda Petr
 */
public final class ExecutionStatus {

    public static final class Component {

        public static enum StatusCode {
            /**
             * Queued for execution. Used as a default value.
             */
            QUEUED,
            /**
             * Component is running.
             */
            RUNNING,
            /**
             * Component completed.
             */
            FINISHED,
            /**
             * Execution failed.
             */
            FAILED,
            /**
             * Component was not executed as it was mapped.
             */
            MAPPED,
            /**
             * Execution of this component was skipped.
             */
            SKIPPED
        };

        private String uri;

        private Progress progress;

        private StatusCode status;

        private Date start;

        private Date end;

        public Component() {

        }

        public Component(String uri) {
            this.uri = uri;
            this.status = StatusCode.QUEUED;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Progress getProgress() {
            return progress;
        }

        public void setProgress(Progress progress) {
            this.progress = progress;
        }

        public StatusCode getStatus() {
            return status;
        }

        public void setStatus(StatusCode status) {
            this.status = status;
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

    }

    private String id;

    private String uri;

    private String pipelineUri;

    private Map<String, Component> components = new HashMap<>();

    private Date executionStart;

    private Date executionEnd;

    private ExecutionStatusCode pipelineStatus;

    private String terminationException;

    private Progress progress;

    private boolean running;

    private Date lastModification;

    /**
     * Size of execution directory in bytes.
     */
    private Long executionDirectorySize = null;

    public ExecutionStatus() {
    }

    public Date getExecutionEnd() {
        return executionEnd;
    }

    public void setExecutionEnd(Date executionEnd) {
        this.executionEnd = executionEnd;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public void setComponents(Map<String, Component> components) {
        this.components = components;
    }

    public Date getExecutionStart() {
        return executionStart;
    }

    public void setExecutionStart(Date executionStart) {
        this.executionStart = executionStart;
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

    public ExecutionStatusCode getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(ExecutionStatusCode pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public String getTerminationException() {
        return terminationException;
    }

    public void setTerminationException(String terminationException) {
        this.terminationException = terminationException;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public Long getExecutionDirectorySize() {
        return executionDirectorySize;
    }

    public void setExecutionDirectorySize(Long executionDirectorySize) {
        this.executionDirectorySize = executionDirectorySize;
    }

}
