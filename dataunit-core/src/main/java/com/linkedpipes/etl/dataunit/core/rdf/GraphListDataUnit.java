package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import java.util.Collection;
import org.eclipse.rdf4j.model.IRI;

/**
 * Utilize one graph (so called "metadata graph") to store references to
 * other graphs, where the data are located. Thus it's possible to work
 * with quads.
 */
public interface GraphListDataUnit extends Rdf4jDataUnit {

    Collection<IRI> getReadGraphs() throws LpException;
}
