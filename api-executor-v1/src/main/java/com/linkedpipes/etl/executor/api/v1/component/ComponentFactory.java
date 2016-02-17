package com.linkedpipes.etl.executor.api.v1.component;

import org.osgi.framework.BundleContext;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 * This class is used to load {@link Component} from bundles.
 *
 * @author Škoda Petr
 */
public interface ComponentFactory {

    /**
     * Used to report that given bundle is not compatible with given {@link ManagerFactory}.
     */
    public class InvalidBundle extends Exception {

        public InvalidBundle(String message) {
            super(message);
        }

        public InvalidBundle(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     *
     * @param definition Access to the SPARQL-like interface of the pipeline definition.
     * @param resoureIri Component IRI.
     * @param graph Name of graph with definition.
     * @param context
     * @return Null if this factory can not be used to create ComponentInstance from given bundle.
     * @throws com.linkedpipes.etl.executor.api.v1.component.ComponentFactory.InvalidBundle
     */
    public Component createComponent(SparqlSelect definition, String resoureIri, String graph, BundleContext context)
            throws InvalidBundle;

}
