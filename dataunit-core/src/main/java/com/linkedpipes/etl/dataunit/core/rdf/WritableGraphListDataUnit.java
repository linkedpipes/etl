package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;

public interface WritableGraphListDataUnit extends Rdf4jDataUnit {

    /**
     * Create a graph and return its IRI.
     */
    IRI createGraph() throws LpException;

}
