package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import org.eclipse.rdf4j.model.IRI;

public interface WritableSingleGraphDataUnit extends Rdf4jDataUnit {

    /**
     * @return Write graph.
     */
    IRI getWriteGraph();

    TripleWriter getWriter();

}
