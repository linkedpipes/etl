package com.linkedpipes.etl.dataunit.sesame.rdf;

import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;

/**
 *
 * @author Petr Škoda
 */
public final class SesameRdfDataUnitFactory {

    public static ManagableSingleGraphDataUnit createSingleGraph(IRI uri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        return new SingleGraphDataUnitImpl(uri, repository, configuration);
    }

    public static ManagableGraphListDataUnit createGraphList(IRI uri, Repository repository,
            RdfDataUnitConfiguration configuration) {
        return new GraphListDataUnitImpl(uri, repository, configuration);
    }

}
