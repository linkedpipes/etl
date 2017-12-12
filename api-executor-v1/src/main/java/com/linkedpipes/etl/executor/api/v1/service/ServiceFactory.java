package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

/**
 * Factory that can be used to instantiate services.
 */
public interface ServiceFactory {

    /**
     * @param serviceType
     * @param component Resource of a component to create service for.
     * @param graph
     * @param definition
     * @param context
     * @return Null if no service of given type can be instantiated.
     */
    Object create(Class<?> serviceType, String component, String graph,
            RdfSource definition, Component.Context context)
            throws LpException;

}
