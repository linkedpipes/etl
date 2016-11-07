package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import org.openrdf.model.IRI;

/**
 * Store all triples in a single graph.
 */
public interface SingleGraphDataUnit extends SesameDataUnit {

    /**
     * @return URI of read graph.
     */
    public IRI getGraph();

}
