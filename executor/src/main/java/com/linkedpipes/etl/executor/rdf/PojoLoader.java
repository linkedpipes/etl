package com.linkedpipes.etl.executor.rdf;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.util.Repositories;

import java.util.ArrayList;
import java.util.List;

public class PojoLoader {

    /**
     * Interface for loadable entities.
     */
    public static interface Loadable {

        /**
         * Report load of the resource IRI.
         *
         * @param iri
         */
        public default void load(String iri) {
            // No operation.
        }

        /**
         * Process given predicate and object. If new object is created and
         * should be loaded then it is returned. In that case the given value
         * is used as a resource URI for loading the object.
         *
         * @param predicate
         * @param object
         * @return Null no new object was created.
         */
        public Loadable load(String predicate, Value object) throws LpException;

        /**
         * Called when the object is loaded. Can be used to finalise loading
         * or perform validation.
         */
        public default void afterLoad() throws LpException {
            // No operation.
        }

    }

    /**
     * Used to store pair of predicate and object.
     */
    private static class PredicateObject {

        private final String predicate;

        private final Value object;

        PredicateObject(String predicate, Value object) {
            this.predicate = predicate;
            this.object = object;
        }

        public String getPredicate() {
            return predicate;
        }

        public Value getObject() {
            return object;
        }

    }

    private PojoLoader() {
    }

    /**
     * @param repository
     * @param resource URI of resource to load.
     * @param graph
     * @param instance Instance to load.
     */
    public static void load(Repository repository, String resource,
            String graph, Loadable instance) throws LpException {
        // Load statements.
        instance.load(resource);
        final List<PredicateObject> records = new ArrayList<>(64);
        try {
            Repositories.consume(repository, (connection) -> {
                final ValueFactory valueFactory
                        = SimpleValueFactory.getInstance();
                final RepositoryResult<Statement> statements
                        = connection.getStatements(
                        valueFactory.createIRI(resource),
                        null, null, false,
                        valueFactory.createIRI(graph));
                while (statements.hasNext()) {
                    final Statement st = statements.next();
                    records.add(new PredicateObject(
                            st.getPredicate().stringValue(),
                            st.getObject()));
                }
            });
        } catch (RepositoryException ex) {
            throw RdfException.failure("Can't load statements.", ex);
        }
        // Parse values.
        for (PredicateObject record : records) {
            final Loadable newInstance = instance.load(record.getPredicate(),
                    record.getObject());
            if (newInstance != null) {
                load(repository, record.getObject().stringValue(),
                        graph, newInstance);
            }
        }
        // Validate entity.
        if (instance == null) {
            return;
        }
        instance.afterLoad();
    }

}
