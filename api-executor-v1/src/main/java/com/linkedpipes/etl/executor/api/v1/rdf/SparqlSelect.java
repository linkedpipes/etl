package com.linkedpipes.etl.executor.api.v1.rdf;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public interface SparqlSelect {

    public static class QueryException extends Exception {

        public QueryException(String message) {
            super(message);
        }

        public QueryException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Evaluate given SPARQL select query and return result in uniform string representation.
     *
     * @param query
     * @return
     * @throws QueryException
     */
    public List<Map<String, String>> executeSelect(String query) throws QueryException;

}
