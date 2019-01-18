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
         */
        List<Map<String, BackendRdfValue>> sparqlSelect(String query)
                throws RdfUtilsException;

    }

    /**
     * Target graph.
     */
    BackendTripleWriter getTripleWriter(String graph);

    /**
     * Must not return null.
     */
    void triples(String graph, TripleHandler handler)
            throws RdfUtilsException;

    /**
     * Save triple to the source.
     *
     * @param resource Can be null.
     * @param graph Must NOT be null.
     * @param handler Called for every triple.
     */
    void triples(String resource, String graph, TripleHandler handler)
            throws RdfUtilsException;

    /**
     * Return source interface that can be used to query the source
     * using SPARQL. Returns Null if interface is not supported.
     */
    SparqlQueryable asQueryable();

}
