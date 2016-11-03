package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.executor.api.v1.RdfException;

import java.util.List;
import java.util.Map;

/**
 * Library independent interface design to query for RDF data.
 */
public interface SparqlSelect {

    /**
     * Evaluate given SPARQL select query and return result in uniform string
     * representation.
     *
     * @param query
     * @return Results of the SPARQL select.
     */
    public List<Map<String, String>> executeSelect(String query)
            throws RdfException;

}
