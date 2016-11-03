package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.util.Map;

/**
 * Base class for loader from RDF to POJO.
 */
abstract class Loader {

    static class CanNotDeserializeObject extends Exception {

        CanNotDeserializeObject(String message) {
            super(message);
        }

        CanNotDeserializeObject(String message, Throwable cause) {
            super(message, cause);
        }

    }

    protected Loader() {
    }

    /**
     * Load property to given object.
     *
     * @param object Object to load into.
     * @param property Record for property to load (iri, value, type, language)
     * @param graph Graph to used for queries.
     * @param select RDF source.
     */
    public abstract void load(Object object, Map<String, String> property,
            String graph, SparqlSelect select) throws CanNotDeserializeObject;

}
