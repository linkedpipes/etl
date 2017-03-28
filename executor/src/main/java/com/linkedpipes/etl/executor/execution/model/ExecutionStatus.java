package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;

enum ExecutionStatus {
    QUEUED(LP_EXEC.STATUS_QUEUED),
    RUNNING(LP_EXEC.STATUS_RUNNING),
    FINISHED(LP_EXEC.STATUS_FINISHED),
    CANCELLED(LP_EXEC.STATUS_CANCELLED),
    CANCELLING(LP_EXEC.STATUS_CANCELLING),
    FAILED(LP_EXEC.STATUS_FAILED);

    private final String iri;

    ExecutionStatus(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

}
