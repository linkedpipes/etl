package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import org.openrdf.model.IRI;


/**
 * Store all triples in a single graph.
 *
 * @author Škoda Petr
 */
public interface SingleGraphDataUnit extends SesameDataUnit {

    /**
     *
     * @return URI of read graph.
     */
    public IRI getGraph();

}
