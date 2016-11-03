package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import org.openrdf.model.IRI;

public interface WritableSingleGraphDataUnit extends SingleGraphDataUnit {

    /**
     * @return URI of read/write graph.
     */
    @Override
    public IRI getGraph();

}
