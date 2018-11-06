package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;

public interface Loadable {

    default void resource(String resource) throws LoaderException {
        // No operation here.
    }

    Loadable load(String predicate, BackendRdfValue value)
            throws RdfUtilsException;

}
