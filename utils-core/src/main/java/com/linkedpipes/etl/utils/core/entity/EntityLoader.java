package com.linkedpipes.etl.utils.core.entity;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import java.util.Map;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Arrays;

/**
 * This class is meant to be used as a helper for loading class from java-RDF. The idea is to use user defined function
 * with string-switch.
 *
 * Advantage to annotations is the pure java code without annotations, bigger flexibility of loading.
 *
 * @author Škoda Petr
 */
public final class EntityLoader {

    public static class LoadingFailed extends LocalizedException {

        public LoadingFailed(String messages, Object... args) {
            super(Arrays.asList(new Message(messages, "en")), args);
        }

    }

    /**
     * Interface for loadable entities.
     */
    public static interface Loadable {

        /**
         * Process given predicate and object. If new object is created and should be loaded then it is returned. In
         * that case the given value is used as a resource URI for loading the object.
         *
         * @param predicate
         * @param value
         * @return Null no new object was created.
         * @throws com.linkedpipes.etl.utils.core.entity.EntityLoader.LoadingFailed
         */
        public Loadable load(String predicate, String value) throws LoadingFailed;

        /**
         * Validation of loaded data can be performed here.
         *
         * @throws com.linkedpipes.etl.utils.core.entity.EntityLoader.LoadingFailed
         */
        public void validate() throws LoadingFailed;

    }

    private EntityLoader() {
    }

    /**
     *
     * @param definition
     * @param resource
     * @param graph
     * @param instance Instance to load.
     * @throws com.linkedpipes.etl.utils.core.entity.EntityLoader.LoadingFailed
     */
    public static void load(SparqlSelect definition, String resource, String graph, Loadable instance)
            throws LoadingFailed {
        final String query = getQuery(resource, graph);
        try {
            for (Map<String, String> record : definition.executeSelect(query)) {
                final Loadable newInstance = instance.load(record.get("p"), record.get("o"));
                if (newInstance != null) {
                    // We have new instance!
                    load(definition, record.get("o"), graph, newInstance);
                }
            }
        } catch (SparqlSelect.QueryException ex) {
            throw new LoadingFailed("Can't execute query for URI: {}", resource, ex);
        }
        // Validate entity.
        instance.validate();
    }

    private static String getQuery(String resource, String graph) {
        final StringBuilder query = new StringBuilder(60);
        query.append("SELECT ?p ?o FROM <");
        query.append(graph);
        query.append("> WHERE { <");
        query.append(resource);
        query.append("> ?p ?o. }");
        return query.toString();
    }

}
