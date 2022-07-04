package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Descriptor;
import com.linkedpipes.etl.rdf.utils.pojo.DescriptorFactory;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RdfUtils {

    private RdfUtils() {

    }

    public static void load(
            BackendRdfSource source, String resource, String graph,
            Loadable loadable) throws RdfUtilsException {
        RdfToPojoLoader loader = new RdfToPojoLoader(source);
        loader.loadResource(resource, graph, loadable);
    }

    public static void load(
            BackendRdfSource source, String resource, String graph,
            Object object, DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        RdfToPojoLoader loader = new RdfToPojoLoader(source);
        loader.loadResourceByReflection(resource, graph, object,
                descriptorFactory);
    }


    public static void loadByType(
            BackendRdfSource source, String graph, Loadable loadable,
            String type) throws RdfUtilsException {
        String resource = getResourceOfType(source, graph, type);
        if (resource == null) {
            throw new RdfUtilsException("Missing resource of given type.");
        }
        load(source, resource, graph, loadable);
    }


    public static void loadByType(
            BackendRdfSource source, String graph,
            String type, Object object, DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        String resource = getResourceOfType(source, graph, type);
        load(source, resource, graph, object, descriptorFactory);
    }

    public static void loadByType(
            BackendRdfSource source, String graph,
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

    private static String getResourceOfType(
            BackendRdfSource source, String graph, String type)
            throws RdfUtilsException {
        List<String> resources = getResourcesOfType(source, graph, type);
        if (resources.size() != 1) {
            throw new InvalidNumberOfResults(
                    "Invalid number of resources ({}) of type: {}",
                    resources.size(), type);
        } else {
            return resources.get(0);
        }
    }

    public static List<String> getResourcesOfType(
            BackendRdfSource source, String graph, String type)
            throws RdfUtilsException {
        if (source instanceof BackendRdfSource.SparqlQueryable) {
            return getResourcesOfTypeByQuery(source, graph, type);
        } else {
            return getResourcesOfTypeByIteration(source, graph, type);
        }
    }

    private static List<String> getResourcesOfTypeByIteration(
            BackendRdfSource source, String graph, String type)
            throws RdfUtilsException {
        List<String> resources = new ArrayList<>();
        source.triples(graph, triple -> {
            if (triple.getPredicate().equals(RDF.TYPE)
                    && triple.getObject().isIri()
                    && triple.getObject().asString().equals(type)) {
                resources.add(triple.getSubject());
            }
        });
        return resources;
    }

    private static List<String> getResourcesOfTypeByQuery(
            BackendRdfSource source, String graph, String type)
            throws RdfUtilsException {
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

    public static <T> List<T> loadList(
            BackendRdfSource source, String graph,
            DescriptorFactory descriptorFactory,
            Class<T> outputType) throws RdfUtilsException {
        String type = descriptorFactory.create(outputType).getObjectType();
        List<String> resources = getResourcesOfType(source, graph, type);
        List<T> output = new LinkedList<>();
        for (String resource : resources) {
            T newEntity = createInstance(outputType);
            load(source, resource, graph, newEntity, descriptorFactory);
            output.add(newEntity);
        }
        return output;
    }

    public static <T extends Loadable> List<T> loadList(
            BackendRdfSource source, String graph, Class<T> outputType,
            String type) throws RdfUtilsException {
        List<String> resources = getResourcesOfType(source, graph, type);
        List<T> output = new LinkedList<>();
        for (String resource : resources) {
            T newEntity = createInstance(outputType);
            load(source, resource, graph, newEntity);
            output.add(newEntity);
        }
        return output;

    }

    private static <T> T createInstance(Class<T> type)
            throws RdfUtilsException {
        try {
            return type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RdfUtilsException("Can't create instance");
        }
    }

    public static String sparqlSelectSingle(
            BackendRdfSource source, String queryAsString, String outputBinding)
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

    public static List<Map<String, String>> sparqlSelect(
            BackendRdfSource source, String queryAsString)
            throws RdfUtilsException {
        BackendRdfSource.SparqlQueryable queryable = source.asQueryable();
        if (queryable == null) {
            throw new RdfUtilsException("Source does not support SPARQL.");
        }
        List<Map<String, String>> output = new LinkedList<>();
        for (Map<String, BackendRdfValue> binding
                : queryable.sparqlSelect(queryAsString)) {
            Map<String, String> outputEntry = new HashMap<>();
            for (Map.Entry<String, BackendRdfValue> entry
                    : binding.entrySet()) {
                outputEntry.put(entry.getKey(), entry.getValue().asString());
            }
            output.add(outputEntry);
        }
        return output;
    }

    public static String sparqlSelectSingleOptional(
            BackendRdfSource source, String queryAsString, String outputBinding)
            throws RdfUtilsException {
        List<Map<String, String>> result = sparqlSelect(source, queryAsString);
        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0).get(outputBinding);
        } else {
            throw new InvalidNumberOfResults(
                    "Invalid number of results: {} (1 expected) for:\n{}",
                    result.size(), queryAsString);
        }
    }

}
