package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;

public interface Loadable {

    default void resource(String resource) throws RdfException {
        // No operation here.
    }

    Loadable load(String predicate, RdfValue value) throws RdfException;

}
