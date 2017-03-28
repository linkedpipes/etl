package com.linkedpipes.etl.dataunit.core.rdf;

import org.eclipse.rdf4j.model.IRI;

public interface WritableSingleGraphDataUnit extends Rdf4jDataUnit {

    /**
     * @return Write graph.
     */
    IRI getWriteGraph();

}
