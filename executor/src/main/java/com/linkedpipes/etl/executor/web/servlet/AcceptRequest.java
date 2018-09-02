package com.linkedpipes.etl.executor.web.servlet;

/**
 * Data transfer object for incoming task definition.
 */
class AcceptRequest {

    /**
     * ExecutionObserver IRI.
     */
    public String iri;

    /**
     * Directory with execution.
     */
    public String directory;

}
