package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Petr Škoda
 */
class LoadObject extends LoaderToValue {

    private final RdfReader.MergeOptionsFactory optionsFactory;

    LoadObject(PropertyDescriptor property, Field field,
            RdfReader.MergeOptionsFactory optionsFactory) {
        super(property, field);
        this.optionsFactory = optionsFactory;
    }


    @Override
    public void load(Object object, Map<String, String> property,
            String graph, SparqlSelect select)
            throws CanNotDeserializeObject {
        // Check object type.
        if (!checkType(field.getType(), select, property.get("value"), graph)) {
            throw new CanNotDeserializeObject("Type missmatch.");
        }
        // Create and set a new object.
        final Object value = loadNew(field.getType(), property.get("value"),
                graph, select, optionsFactory);
        set(object, value, property.get("iri"));
    }

    /**
     * From RDF load a object of given type.
     *
     * @param type
     * @param iri
     * @param graph
     * @param select
     * @param optionsFactory
     * @return Null if object of given type can't be created.
     */
    static Object loadNew(Class<?> type, String iri, String graph,
            SparqlSelect select, RdfReader.MergeOptionsFactory optionsFactory
    ) throws CanNotDeserializeObject {
        // Create a new instance.
        final Object value;
        try {
            value = type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new CanNotDeserializeObject("Can't create object instance.",
                    ex);
        }
        // Load to instance and return it.
        loadToObject(value, iri, graph, select, optionsFactory);
        return value;
    }

    /**
     * @param object Object to load data into.
     * @param iri Resource of an object to load.
     * @param graph Graph that should be used to load the object from.
     * @param select RDF source.
     * @param optionsFactory
     */
    static void loadToObject(Object object, String iri, String graph,
            SparqlSelect select, RdfReader.MergeOptionsFactory optionsFactory)
            throws CanNotDeserializeObject {
        // Load properties.
        final List<Map<String, String>> results;
        try {
            results = select.executeSelect(
                    getQueryForProperties(iri, graph));
        } catch (RdfException ex) {
            throw new CanNotDeserializeObject("Can't query source.", ex);
        }
        // Generate description.
        final DescriptionFactory descFactory = new DescriptionFactory();
        final Map<String, List<Loader>> loaders
                = descFactory.createDescription(object.getClass(),
                optionsFactory);
        //
        final RdfReader.MergeOptions mergeOptions;
        try {
            mergeOptions = optionsFactory.create(iri, graph);
        } catch (RdfException ex) {
            throw new CanNotDeserializeObject(
                    "Can't load configuration description.", ex);
        }
        // Load data - for each property.
        for (Map<String, String> item : results) {
            // Check if we can load the predicate.
            if (!loaders.containsKey(item.get("iri"))) {
                continue;
            }
            // Check if we should load the predicate.
            if (!mergeOptions.load(item.get("iri"))) {
                continue;
            }
            // Test all loaders.
            for (Loader loader : loaders.get(item.get("iri"))) {
                try {
                    loader.load(object, item, graph, select);
                    break;
                } catch (CanNotDeserializeObject ex) {
                    // This loader can not be used to load the object.
                }
            }
        }
    }

    private static boolean checkType(Class<?> type, SparqlSelect select,
            String iri, String graph) throws CanNotDeserializeObject {
        final RdfToPojo.Type annotationType
                = type.getAnnotation(RdfToPojo.Type.class);
        if (annotationType == null) {
            throw new CanNotDeserializeObject("Missing annotation.");
        }
        final List<Map<String, String>> results;
        try {
            results = select.executeSelect(
                    getQueryForTypes(iri, graph));
        } catch (RdfException ex) {
            throw new CanNotDeserializeObject("Can't query source.", ex);
        }
        boolean typeMatch = false;
        for (Map<String, String> result : results) {
            if (result.get("type").compareTo(annotationType.uri()) == 0) {
                typeMatch = true;
                break;
            }
        }
        return typeMatch;
    }

    private static String getQueryForProperties(String iri, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?iri ?value ?type ?language ");
        if (graph != null) {
            query.append("FROM <");
            query.append(graph);
            query.append("> ");
        }
        query.append(" WHERE { \n <");
        query.append(iri);
        query.append("> ?iri ?value. \n");
        query.append(" BIND(datatype(?value) AS ?type)");
        query.append(" BIND(lang(?value) AS ?language) \n");
        query.append("}");
        return query.toString();
    }

    private static String getQueryForTypes(String iri, String graph) {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ?type ");
        if (graph != null) {
            query.append("FROM <");
            query.append(graph);
            query.append("> ");
        }
        query.append(" WHERE { \n <");
        query.append(iri);
        query.append("> a ?type . }");
        return query.toString();
    }

}
