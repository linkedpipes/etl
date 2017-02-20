package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

@FunctionalInterface
public interface EntityControlFactory {

    /**
     * @param type
     * @return Control for object of given type.
     */
    EntityControl create(String type) throws RdfUtilsException;

}
