package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;

public interface Loadable {

    default void resource(String resource) throws LoaderException {
        // No operation here.
    }

    Loadable load(String predicate, RdfValue value) throws RdfUtilsException;

}
