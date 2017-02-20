package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;

import java.util.Collection;

/**
 * Utilize one graph (so called "metadata graph") to store references to
 * other graphs, where the data are located. Thus it's possible to work
 * with quads.
 */
public interface GraphListDataUnit extends Rdf4jDataUnit {

    Collection<IRI> getReadGraphs() throws LpException;

}
