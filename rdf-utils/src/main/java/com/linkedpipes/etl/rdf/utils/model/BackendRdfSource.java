package com.linkedpipes.etl.rdf.utils.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.List;
import java.util.Map;

public interface BackendRdfSource {

    /**
     * Add ability to query the source with SPARQL.
     */
    interface SparqlQueryable {

        /**
         * Execute given SPARQL select query and return result.
         *
         * @param query
         * @return
         */
        List<Map<String, BackendRdfValue>> sparqlSelect(String query)
                throws RdfUtilsException;

    }

    /**
     * @param graph Target graph.
     * @return
     */
    BackendTripleWriter getTripleWriter(String graph);

    /**
     * @param graph Must NOT be null.
     * @param handler
     */
    void triples(String graph, TripleHandler handler)
            throws RdfUtilsException;

    /**
     * @param resource Can be null.
     * @param graph Must NOT be null.
     * @param handler
     */
    void triples(String resource, String graph, TripleHandler handler)
            throws RdfUtilsException;

    /**
     * @return Null is SPARQL is not supported.
     */
    SparqlQueryable asQueryable();

}
