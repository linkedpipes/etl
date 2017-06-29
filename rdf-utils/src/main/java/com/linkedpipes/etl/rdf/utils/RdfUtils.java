package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Descriptor;
import com.linkedpipes.etl.rdf.utils.pojo.DescriptorFactory;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.*;

public class RdfUtils {

    private RdfUtils() {

    }

    public static void load(RdfSource source, String resource, String graph,
            Loadable loadable) throws RdfUtilsException {
        RdfToPojoLoader loader = new RdfToPojoLoader(source);
        loader.loadResource(resource, graph, loadable);
    }

    public static void loadByType(RdfSource source, String graph,
            Loadable loadable, String type) throws RdfUtilsException {
        String resource = getResourceOfType(source, graph, type);
        if (resource == null) {
            throw new RdfUtilsException("Missing resource of given type.");
        }
        load(source, resource, graph, loadable);
    }

    private static String getResourceOfType(RdfSource source, String graph,
            String type) throws RdfUtilsException {
        List<String> resources = getResourcesOfType(source, graph, type);
        if (resources.size() != 1) {
            throw new InvalidNumberOfResults(
                    "Invalid number of resources ({}) of type: {}",
                    resources.size(), type);
        } else {
            return resources.get(0);
        }
    }

    public static List<String> getResourcesOfType(RdfSource source,
            String graph, String type) throws RdfUtilsException {
        if (source instanceof RdfSource.SparqlQueryable) {
            return getResourcesOfTypeByQuery(source, graph, type);
        } else {
            return getResourcesOfTypeByIteration(source, graph, type);
        }
    }

    private static List<String> getResourcesOfTypeByIteration(RdfSource source,
            String graph, String type) throws RdfUtilsException {
        List<String> resources = new ArrayList<>();
        source.triples(graph, triple -> {
            if (triple.getPredicate().equals(RDF.TYPE) &&
                    triple.getObject().isIri() &&
                    triple.getObject().asString().equals(type)) {
                resources.add(triple.getSubject());
            }
        });
        return resources;
    }

    private static List<String> getResourcesOfTypeByQuery(RdfSource source,
            String graph, String type) throws RdfUtilsException {
        String queryAsString = "SELECT ?s ";
        if (graph != null) {
            queryAsString += " FROM <" + graph + "> ";
        }
        queryAsString += " WHERE { ?s a <" + type + "> } ";
        List<String> resources = new ArrayList<>();
        for (Map<String, String> binding
                : sparqlSelect(source, queryAsString)) {
            resources.add(binding.get("s"));
        }
        return resources;
    }

    public static void load(RdfSource source, String resource, String graph,
            Object object, DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        RdfToPojoLoader loader = new RdfToPojoLoader(source);
        loader.loadResourceByReflection(resource, graph, object,
                descriptorFactory);
    }

    public static void loadByType(RdfSource source, String graph,
            String type, Object object, DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        String resource = getResourceOfType(source, graph, type);
        load(source, resource, graph, object, descriptorFactory);
    }

    public static void loadByType(RdfSource source, String graph,
            Object object, DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        Descriptor descriptor = descriptorFactory.create(object.getClass());
        if (descriptor == null) {
            throw new RdfUtilsException("Can't get descriptor.");
        }
        String type = descriptor.getObjectType();
        String resource = getResourceOfType(source, graph, type);
        load(source, resource, graph, object, descriptorFactory);
    }

    public static <Type> List<Type> loadList(RdfSource source, String graph,
            DescriptorFactory descriptorFactory,
            Class<Type> outputType) throws RdfUtilsException {
        String type = descriptorFactory.create(outputType).getObjectType();
        List<String> resources = getResourcesOfType(source, graph, type);
        List<Type> output = new LinkedList<>();
        for (String resource : resources) {
            Type newEntity = createInstance(outputType);
            load(source, resource, graph, newEntity, descriptorFactory);
            output.add(newEntity);
        }
        return output;
    }

    private static <Type> Type createInstance(Class<Type> type)
            throws RdfUtilsException {
        try {
            return type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RdfUtilsException("Can't create instance");
        }
    }

    public static <Type extends Loadable> List<Type> loadList(RdfSource source,
            String graph, Class<Type> outputType, String type)
            throws RdfUtilsException {
        List<String> resources = getResourcesOfType(source, graph, type);
        List<Type> output = new LinkedList<>();
        for (String resource : resources) {
            Type newEntity = createInstance(outputType);
            load(source, resource, graph, newEntity);
            output.add(newEntity);
        }
        return output;

    }

    public static String sparqlSelectSingle(RdfSource source,
            String queryAsString, String outputBinding)
            throws RdfUtilsException {
        List<Map<String, String>> result = sparqlSelect(source, queryAsString);
        if (result.size() != 1) {
            throw new InvalidNumberOfResults(
                    "Invalid number of results: {} (1 expected) for:\n{}",
                    result.size(), queryAsString);
        } else {
            return result.get(0).get(outputBinding);
        }
    }

    public static List<Map<String, String>> sparqlSelect(RdfSource source,
            String queryAsString) throws RdfUtilsException {
        RdfSource.SparqlQueryable queryable = source.asQueryable();
        if (queryable == null) {
            throw new RdfUtilsException("Source does not support SPARQL.");
        }
        List<Map<String, String>> output = new LinkedList<>();
        for (Map<String, RdfValue> binding
                : queryable.sparqlSelect(queryAsString)) {
            Map<String, String> outputEntry = new HashMap<>();
            for (Map.Entry<String, RdfValue> entry
                    : binding.entrySet()) {
                outputEntry.put(entry.getKey(), entry.getValue().asString());
            }
            output.add(outputEntry);
        }
        return output;
    }

}
