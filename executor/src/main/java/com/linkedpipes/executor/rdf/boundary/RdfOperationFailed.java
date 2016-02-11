package com.linkedpipes.executor.rdf.boundary;

/**
 *
 * @author Škoda Petr
 */
public class RdfOperationFailed extends Exception {

    public RdfOperationFailed(String message) {
        super(message);
    }

    public RdfOperationFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
