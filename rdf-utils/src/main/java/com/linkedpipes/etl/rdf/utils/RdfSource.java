package com.linkedpipes.etl.rdf.utils;

import java.util.List;
import java.util.Map;

/**
 * Represent a generic RDF source. Via the getter method the source
 * may provide access to selected functionality.
 */
public interface RdfSource<ValueType> {

    /**
     * Interface for writing RDF triples.
     */
    interface TripleWriter {

        void iri(String s, String p, String o);

        /**
         * @param s
         * @param p
         * @param o
         * @param type Literal type.
         */
        void typed(String s, String p, String o, String type);

        /**
         * @param s
         * @param p
         * @param o
         * @param language Can be null in such case no language tag is used.
         */
        void string(String s, String p, String o, String language);

        /**
         * Submit added statements to the {@link RdfSource} and delete inner
         * state.
         *
         * Can be called multiple times.
         */
        void submit() throws RdfUtilsException;

    }

    interface TypedTripleWriter<ValueType> extends TripleWriter {

        void add(String s, String p, ValueType o);

    }

    @FunctionalInterface
    interface TripleHandler<T> {

        void handle(String s, String p, T o) throws RdfUtilsException;

    }

    /**
     * Provide information about the value type of the interface.
     *
     * @param <T>
     */
    interface ValueInfo<T> {

        boolean isIri(T value);

    }

    /**
     * Convert value to given type.
     */
    interface ValueConverter<T> {

        Boolean asBoolean(T value);

        Integer asInteger(T value);

        Long asLong(T value);

        Float asFloat(T value);

        Double asDouble(T value);

        String asString(T value);

        /**
         * @param value
         * @return Language tag or null if it's missing.
         */
        String langTag(T value);

    }

    /**
     * Special interface used to convert values to String.
     */
    @FunctionalInterface
    interface ValueToString<T> {

        String asString(T value);

    }

    /**
     * @return Default value type.
     */
    Class<ValueType> getDefaultType();

    /**
     * Shutdown repository
     */
    void shutdown();

    /**
     * @param graph
     * @return Writer interface for this object, can return this object.
     */
    TripleWriter getTripleWriter(String graph);

    /**
     * @param graph
     * @return Typed version of {@link #getTripleWriter(String)};
     */
    TypedTripleWriter<ValueType> getTypedTripleWriter(String graph);

    /**
     * @return Object that can be used to examine the default value objects.
     */
    ValueInfo<ValueType> getValueInfo();

    /**
     * Iterate over triples with given subject in given graph.
     *
     * @param resource Must not be null.
     * @param graph
     * @param clazz
     * @param handler
     */
    <T> void triples(String resource, String graph,
            Class<T> clazz, TripleHandler<T> handler)
            throws RdfUtilsException;

    /**
     * @return Converted of used value type.
     */
    ValueConverter<ValueType> valueConverter();

    /**
     * @param clazz
     * @param <T>
     * @return Converter from given type to string.
     */
    <T> ValueToString<T> toStringConverter(Class<T> clazz);

    /**
     * Execute given SPARQL select query and return result.
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<Map<String, T>> sparqlSelect(String query, Class<T> clazz)
            throws RdfUtilsException;
}
