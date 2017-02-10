package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

import java.util.List;
import java.util.Map;

/**
 * Main access point to the RDF triple utilities.
 */
public class RdfUtils {

    /**
     * @param source
     * @param object Object to load.
     * @param resource Resource of openEntity to load.
     * @param graph
     * @param clazz
     * @param <ValueType>
     */
    public static <ValueType> void load(RdfSource source,
            RdfLoader.Loadable<ValueType> object, String resource, String graph,
            Class<ValueType> clazz) throws RdfUtilsException {
        RdfLoader.load(source, object, resource, graph, clazz);
    }

    /**
     * @param source
     * @param object
     * @param graph
     * @param descriptorFactory
     */
    public static void loadTypedByReflection(RdfSource source, Object object,
            String graph, RdfLoader.DescriptorFactory descriptorFactory)
            throws RdfUtilsException {
        final RdfLoader.Descriptor descriptor =
                descriptorFactory.create(object.getClass());
        final String query = "SELECT ?s WHERE { GRAPH <" + graph + "> { " +
                "?s a <" + descriptor.getType() + "> } }";
        final String resource = sparqlSelectSingle(source, query, "s");
        RdfLoader.loadByReflection(source, descriptorFactory, object,
                resource, graph);
    }

    /**
     * Execute given query and return the result. If the number of results
     * is not one, then throws an exception.
     *
     * @param source
     * @param queryAsString
     * @return
     */
    public static String sparqlSelectSingle(RdfSource source,
            String queryAsString, String binding) throws RdfUtilsException {
        final List<Map<String, String>> result =
                source.sparqlSelect(queryAsString, String.class);
        if (result.size() != 1) {
            throw new RdfUtilsException(
                    "Invalid number of results: {} (1 expected) for:\n{}",
                    result.size(), queryAsString);
        }
        return result.get(0).get(binding);
    }

    public static List<Map<String, String>> sparqlSelect(RdfSource source,
            String queryAsString) throws RdfUtilsException {
        return source.sparqlSelect(queryAsString, String.class);
    }

    /**
     * Recursively copy entity from source to target.
     *
     * @param iri
     * @param sourceGraph
     * @param source
     * @param writer Caller is responsible for transaction handling.
     * @param type
     * @param <ValueType>
     */
    public static <ValueType> void copyEntityRecursive(String iri,
            String sourceGraph, RdfSource<ValueType> source,
            RdfSource.TypedTripleWriter<ValueType> writer,
            Class<ValueType> type)
            throws RdfUtilsException {
        final RdfSource.ValueInfo<ValueType> info = source.getValueInfo();
        final RdfSource.ValueToString<ValueType> convert =
                source.toStringConverter(type);
        source.triples(iri, sourceGraph, type, (s, p, o) -> {
            writer.add(s, p, o);
            if (info.isIri(o)) {
                copyEntityRecursive(convert.asString(o),
                        sourceGraph, source, writer, type);
            }
        });
    }

}
