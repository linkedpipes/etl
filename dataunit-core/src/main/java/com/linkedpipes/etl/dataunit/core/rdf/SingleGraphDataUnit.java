package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import org.eclipse.rdf4j.model.IRI;

/**
 * Store all triples in a single graph.
 *
 * <p>Can be used as a runtime configuration.
 */
public interface SingleGraphDataUnit extends Rdf4jDataUnit {

    IRI getReadGraph();

    RdfSource asRdfSource();

}
