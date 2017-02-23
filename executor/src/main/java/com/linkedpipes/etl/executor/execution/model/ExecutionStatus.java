package com.linkedpipes.etl.executor.execution.model;

enum ExecutionStatus {
    NONE("http://etl.linkedpipes.com/resources/status/none"),
    MAPPED("http://etl.linkedpipes.com/resources/status/mapped"),
    QUEUED("http://etl.linkedpipes.com/resources/status/queued"),
    INITIALIZING(
            "http://etl.linkedpipes.com/resources/status/initializing"),
    RUNNING("http://etl.linkedpipes.com/resources/status/running"),
    FINISHED("http://etl.linkedpipes.com/resources/status/finished"),
    CANCELLED("http://etl.linkedpipes.com/resources/status/cancelled"),
    CANCELLING("http://etl.linkedpipes.com/resources/status/cancelling"),
    FAILED("http://etl.linkedpipes.com/resources/status/failed");

    private final String iri;

    ExecutionStatus(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

}
