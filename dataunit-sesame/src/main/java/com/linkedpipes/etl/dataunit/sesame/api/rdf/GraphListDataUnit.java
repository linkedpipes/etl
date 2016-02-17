package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import java.util.Collection;

import org.openrdf.model.IRI;


/**
 * Utilize one graph (so called "metadata graph") to store references to other graphs, where the data are located.
 * Thus it's possible to work with quads.
 *
 * @author Škoda Petr
 */
public interface GraphListDataUnit extends SesameDataUnit {

    public Collection<IRI> getGraphs() throws SesameDataUnitException;

}
