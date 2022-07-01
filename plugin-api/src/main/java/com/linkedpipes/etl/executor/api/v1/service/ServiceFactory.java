package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

/**
 * Factory that can be used to instantiate services.
 */
public interface ServiceFactory {

    /**
     * Create a service.
     *
     * @param serviceType Required service class.
     * @param component   Resource of a component to create service for.
     * @param definition  Access to RDF definition.
     * @param context     Component context.
     * @return Null if no service of given type can be instantiated.
     */
    Object create(
            Class<?> serviceType, String component, RdfSource definition,
            Component.Context context)
            throws LpException;

}
