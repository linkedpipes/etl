package com.linkedpipes.etl.dataunit.sesame.rdf;

import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;

/**
 *
 * @author Petr Škoda
 */
public final class SesameRdfDataUnitFactory {

    public SesameRdfDataUnitFactory() {
    }

    public static ManagableSingleGraphDataUnit createSingleGraph(IRI iri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        return new SingleGraphDataUnitImpl(iri, repository, configuration);
    }

    public static ManagableGraphListDataUnit createGraphList(IRI iri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        return new GraphListDataUnitImpl(iri, repository, configuration);
    }

}
