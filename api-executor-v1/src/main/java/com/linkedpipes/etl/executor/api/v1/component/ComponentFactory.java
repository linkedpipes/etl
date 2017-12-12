package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import org.osgi.framework.BundleContext;

/**
 * A factory for creating components. The aim of the factory is
 * to search for the bundle for object of {@link Component} (by default)
 * and return a wrap for this object as {@link ManageableComponent}.
 */
public interface ComponentFactory {

    /**
     * Throw an exception if instantiation of a component failed.
     *
     * @param component
     * @param graph
     * @param definition
     * @param bundleContext
     * @return Null if the bundle does not contains instantiable component.
     */
    ManageableComponent create(String component, String graph,
            RdfSource definition, BundleContext bundleContext) throws
            LpException;

}
