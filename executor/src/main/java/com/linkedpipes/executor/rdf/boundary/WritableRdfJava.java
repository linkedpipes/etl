package com.linkedpipes.executor.rdf.boundary;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;

import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.executor.rdf.controller.ConnectionAction;
import org.openrdf.model.URI;

/**
 * Extension of interface for statement addition to support begin, commit operation for better performance.
 *
 * @author Škoda Petr
 */
public final class WritableRdfJava implements StatementWriter {

    private final Repository repository;

    private final ValueFactory valueFactory;

    private final List<Statement> statemets = new ArrayList<>(16);

    private final URI graph;

    public WritableRdfJava(Repository repository, String graph) {
        this.repository = repository;
        this.valueFactory = repository.getValueFactory();
        this.graph = this.valueFactory.createURI(graph);
    }

    public void begin() {
        // No operation here!
    }

    public void commit() throws RdfOperationFailed {
        try {
            ConnectionAction.call(repository, (connection) -> {
                connection.begin();
                connection.add(statemets);
                connection.commit();
            });
        } catch (ConnectionAction.CallFailed ex) {
            throw new RdfOperationFailed("Can't submit data.", ex);
        } finally {
            statemets.clear();
        }
    }

    @Override
    public void add(String subject, String predicate, String object, String type) {
        statemets.add(valueFactory.createStatement(
                valueFactory.createURI(subject),
                valueFactory.createURI(predicate),
                valueFactory.createLiteral(object, valueFactory.createURI(type)),
                graph));
    }

    @Override
    public void addUri(String subject, String predicate, String object) {
        statemets.add(valueFactory.createStatement(
                valueFactory.createURI(subject),
                valueFactory.createURI(predicate),
                valueFactory.createURI(object),
                graph));
    }

    @Override
    public void addString(String subject, String predicate, String object, String language) {
        if (language == null) {
            statemets.add(valueFactory.createStatement(
                    valueFactory.createURI(subject),
                    valueFactory.createURI(predicate),
                    valueFactory.createLiteral(object),
                    graph));
        } else {
            statemets.add(valueFactory.createStatement(
                    valueFactory.createURI(subject),
                    valueFactory.createURI(predicate),
                    valueFactory.createLiteral(object, language),
                    graph));
        }
    }

}
