package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Map;

/**
 * Facade used to make the functionality of this package public.
 *
 * @author Petr Škoda
 */
public class RdfReader {

    private RdfReader() {
    }

    /**
     * Automatically detect entities that can be loaded into given object.
     *
     * @param object
     * @param source
     * @param graph
     * @throws RdfException
     */
    public static void addToObject(Object object, SparqlSelect source,
            String graph) throws RdfException {
        // Search for entities.
        final String typeAsString = getObjectType(object);
        final String query;
        if (graph == null) {
            query = getQueryForTypes(typeAsString);
        } else {
            query = getQueryForTypes(typeAsString, graph);
        }
        // Load for all reasources.
        for (Map<String, String> configPair : source.executeSelect(query)) {
            if (graph == null) {
                addToObject(object, source, configPair.get("g"),
                        configPair.get("s"));
            } else {
                addToObject(object, source, graph, configPair.get("s"));
            }
        }
    }

    /**
     * Load resource of given IRI to given object.
     *
     * @param object
     * @param source
     * @param graph
     * @param resourceIri
     * @throws RdfException
     */
    public static void addToObject(Object object, SparqlSelect source,
            String graph, String resourceIri) throws RdfException {
        try {
            LoadObject.loadToObject(object, resourceIri, graph, source);
        } catch (Loader.CanNotDeserializeObject ex) {
            throw RdfException.failure("Can't load entity.", ex);
        }
    }

    private static String getObjectType(Object object) throws RdfException {
        final RdfToPojo.Type type
                = object.getClass().getAnnotation(RdfToPojo.Type.class);
        if (type == null) {
            throw RdfException.failure("Missing type annotation for: {}",
                    object.getClass().getName());
        }
        return type.uri();
    }

    /**
     * Output binding:
     * <ul>
     * <li>s - resources URI.</li>
     * <li>g - graph name.</li>
     * </ul>
     *
     * @param type
     * @param graph If null GRAPHT statement is not used.
     * @return Query that search for objects of given types.
     */
    static String getQueryForTypes(String type) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?s ?g ");
        query.append("WHERE { GRAPH ?g {?s a <");
        query.append(type);
        query.append("> } }");
        return query.toString();
    }

    /**
     * Output binding:
     * <ul>
     * <li>s - resources URI.</li>
     * </ul>
     *
     * @param type
     * @param graph If null GRAPHT statement is not used.
     * @return Query that search for objects of given types.
     */
    static String getQueryForTypes(String type, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?s ?g ");
        query.append("FROM <");
        query.append(graph);
        query.append("> ");
        query.append("WHERE { ?s a <");
        query.append(type);
        query.append("> }");
        return query.toString();
    }

}
