package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import org.osgi.framework.BundleContext;

/**
 * Component factory. Is used to create components from OSGI bundles.
 */
public interface ComponentFactory {

    /**
     * @param definition
     * @param resourceIri
     * @param graph
     * @param bundleContext
     * @param context
     * @return Null if there is no component for this factory in the bundle.
     */
    public Component create(SparqlSelect definition, String resourceIri,
            String graph, BundleContext bundleContext,
            Component.Context context) throws RdfException;

}
