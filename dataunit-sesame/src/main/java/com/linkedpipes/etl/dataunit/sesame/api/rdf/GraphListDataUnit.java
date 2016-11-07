package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;

import java.util.Collection;

/**
 * Utilize one graph (so called "metadata graph") to store references to
 * other graphs, where the data are located. Thus it's possible to work
 * with quads.
 */
public interface GraphListDataUnit extends SesameDataUnit {

    public Collection<IRI> getGraphs() throws LpException;

}
