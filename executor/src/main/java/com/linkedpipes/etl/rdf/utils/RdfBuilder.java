package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

/**
 * Can be used to produce RDF statements.
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

        public EntityBuilder string(
                String predicate, String value, String language) {
            writer.string(resource, predicate, value, language);
            return this;
        }

        public EntityBuilder integer(String predicate, int value) {
            writer.typed(resource, predicate,
                    Integer.toString(value), XSD.INTEGER);
            return this;
        }

        public EntityBuilder bool(String predicate, boolean value) {
            writer.typed(resource, predicate,
                    Boolean.toString(value), XSD.BOOLEAN);
            return this;
        }

        public EntityBuilder typed(String predicate, String value, String type)
                throws RdfUtilsException {
            writer.typed(resource, predicate, value, type);
            return this;
        }

        public EntityBuilder close() {
            return parent;
        }

    }

    private final BackendTripleWriter writer;

    private RdfBuilder(BackendTripleWriter writer) {
        this.writer = writer;
    }

    public EntityBuilder entity(String resource) {
        return new EntityBuilder(null, resource);
    }

    /**
     * Add triples into the source.
     */
    public void commit() throws RdfUtilsException {
        writer.flush();
    }

    public static RdfBuilder create(BackendRdfSource source, String graph) {
        BackendTripleWriter writer = source.getTripleWriter(graph);
        return new RdfBuilder(writer);
    }

}
