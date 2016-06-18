package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public interface SparqlSelect {

    /**
     * Evaluate given SPARQL select query and return result in uniform string
     * representation.
     *
     * @param query
     * @return
     * @throws com.linkedpipes.etl.executor.api.v1.RdfException
     */
    public List<Map<String, String>> executeSelect(String query)
            throws RdfException;

}
