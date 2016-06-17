package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public interface SparqlSelect {

    public static class QueryException extends LocalizedException {

        public QueryException(String messages, Object... args) {
            super(Arrays.asList(new Message(messages, "en")), args);
        }

    }

    /**
     * Evaluate given SPARQL select query and return result in uniform string
     * representation.
     *
     * @param query
     * @return
     * @throws QueryException
     */
    public List<Map<String, String>> executeSelect(String query)
            throws QueryException;

}
