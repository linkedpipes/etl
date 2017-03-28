package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

/**
 * Can be used to produce RDF statements.
 *
 * The builder must be closed to submit the transaction.
 */
public class RdfBuilder {

    public class EntityBuilder {

        private final EntityBuilder parent;

        private final String resource;

        protected EntityBuilder(EntityBuilder parent, String resource) {
            this.parent = parent;
            this.resource = resource;
        }

        public EntityBuilder entity(String predicate, String resource) {
            writer.iri(this.resource, predicate, resource);
            return new EntityBuilder(this, resource);
        }

        public EntityBuilder iri(String predicate, String value) {
            writer.iri(resource, predicate, value);
            return this;
        }

        public EntityBuilder string(String predicate, String value) {
            writer.string(resource, predicate, value, null);
            return this;
        }

        public EntityBuilder string(String predicate, String value,
                String language) {
            writer.string(resource, predicate, value, language);
            return this;
        }

        public EntityBuilder integer(String predicate, int value) {
            writer.typed(resource, predicate, Integer.toString(value),
                    XSD.INTEGER);
            return this;
        }

        public EntityBuilder bool(String predicate, boolean value) {
            writer.typed(resource, predicate, Boolean.toString(value),
                    XSD.BOOLEAN);
            return this;
        }


        /**
         * Close currently open openEntity.
         *
         * @return
         */
        public EntityBuilder close() {
            return parent;
        }

    }

    private final RdfSource.TripleWriter writer;

    public RdfBuilder(RdfSource.TripleWriter writer) {
        this.writer = writer;
    }

    public EntityBuilder entity(String resource) {
        return new EntityBuilder(null, resource);
    }

    /**
     * Close the writer, must be called at the end of {@link RdfBuilder}
     * usage.
     */
    public void commit() throws RdfUtilsException {
        writer.submit();
    }

    /**
     * Create a model that writes data to given {@link RdfSource}.
     *
     * @param source
     * @return
     */
    public static RdfBuilder create(RdfSource source, String graph)
            throws RdfUtilsException {
        final RdfSource.TripleWriter writer = source.getTripleWriter(graph);
        if (writer == null) {
            throw new RdfUtilsException(
                    "Source does not provide TripleWriter interface.");
        }
        return new RdfBuilder(writer);
    }

}
