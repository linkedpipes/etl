package com.linkedpipes.etl.executor.monitor.execution;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

// TODO Consider split into two enums ExecutorStatus, ExecutorMonitorStatus.
public enum ExecutionStatus {
    /**
     * Queued for an execution.
     */
    QUEUED("http://etl.linkedpipes.com/resources/status/queued"),
    /**
     * Running with executor.
     */
    RUNNING("http://etl.linkedpipes.com/resources/status/running"),
    /**
     * Finished.
     */
    FINISHED("http://etl.linkedpipes.com/resources/status/finished"),
    /**
     * Finished.
     */
    FAILED("http://etl.linkedpipes.com/resources/status/failed"),
    /**
     * Running but with no known executor. Or the executor was known
     * but changed the executing pipeline.
     */
    DANGLING("http://etl.linkedpipes.com/resources/status/dangling"),
    /**
     * Pipeline was running with executor but the executor is
     * unresponsive.
     */
    UNRESPONSIVE("http://etl.linkedpipes.com/resources/status/unresponsive"),
    /**
     * Deleted execution, represents a tombstone.
     */
    DELETED("http://etl.linkedpipes.com/resources/status/deleted"),
    /**
     * When we do not know.
     */
    UNKNOWN("http://etl.linkedpipes.com/resources/status/unknown");

    private IRI iri;

    ExecutionStatus(String iriAsStr) {
        this.iri = SimpleValueFactory.getInstance().createIRI(iriAsStr);
    }

    public IRI asIri() {
        return this.iri;
    }

    public String asStr() {
        return this.iri.stringValue();
    }

    public static ExecutionStatus fromIri(String iriAsStr) {
        for (ExecutionStatus status : ExecutionStatus.values()) {
            if (status.iri.stringValue().equals(iriAsStr)) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public static boolean isFinished(ExecutionStatus status) {
        return status == FINISHED || status == FAILED;
    }

}

