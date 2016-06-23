package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Map;

/**
 * Base for loading RDF to POJO.
 *
 * @author Petr Škoda
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
     * @param object
     * @param property
     * @param graph
     * @param select
     * @throws CanNotDeserializeObject
     */
    public abstract void load(Object object, Map<String, String> property,
            String graph, SparqlSelect select) throws CanNotDeserializeObject;

}
