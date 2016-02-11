package com.linkedpipes.executor.rdf.boundary;

/**
 *
 * @author Škoda Petr
 */
public class QueryContainer {

    private final Exception exception;

    private final String payload;

    public QueryContainer(Exception exception) {
        this.exception = exception;
        this.payload = null;
    }

    public QueryContainer(String payload) {
        this.exception = null;
        this.payload = payload;
    }

    public Exception getException() {
        return exception;
    }

    public String getPayload() {
        return payload;
    }

}
