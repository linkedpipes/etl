package com.linkedpipes.etl.dataunit.core.rdf;

import org.eclipse.rdf4j.model.IRI;

/**
 * Store all triples in a single graph.
 */
public interface SingleGraphDataUnit extends Rdf4jDataUnit {

    /**
     * @return URI of read graph.
     */
    IRI getReadGraph();

}
