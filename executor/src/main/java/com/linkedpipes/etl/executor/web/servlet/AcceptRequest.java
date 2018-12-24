package com.linkedpipes.etl.executor.web.servlet;

/**
 * Data transfer object for incoming task definition.
 */
class AcceptRequest {

    /**
     * ExecutionObserver IRI.
     */
    private String iri;

    /**
     * Directory with execution.
     */
    private String directory;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
