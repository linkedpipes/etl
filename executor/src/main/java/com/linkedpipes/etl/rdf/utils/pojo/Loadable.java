package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

public interface Loadable {

    default void resource(String resource) throws LoaderException {
        // No operation here.
    }

    Loadable load(String predicate, BackendRdfValue value)
            throws RdfUtilsException;

}
