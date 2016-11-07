package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;

public interface WritableGraphListDataUnit extends GraphListDataUnit {

    public IRI createGraph() throws LpException;

}
