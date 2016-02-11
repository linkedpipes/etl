package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import org.openrdf.model.IRI;

/**
 *
 * @author Škoda Petr
 */
public interface WritableGraphListDataUnit extends GraphListDataUnit {

    public IRI createGraph() throws SesameDataUnitException;

}
