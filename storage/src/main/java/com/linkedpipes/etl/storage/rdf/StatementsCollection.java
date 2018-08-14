package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.*;
import java.util.function.Predicate;

public class StatementsCollection {

    private final Collection<Statement> statements;

    /**
     * Operate on given collection.
     *
     * @param statements
     */
    public StatementsCollection(Collection<Statement> statements) {
        this.statements = statements;
    }

    public void addAll(StatementsCollection toAdd) {
        statements.addAll(toAdd.statements);
    }

    public void addAll(Collection<Statement> collection) {
        statements.addAll(collection);
    }

    public StatementsCollection filter(Predicate<Statement> filter) {
        final List<Statement> result = new LinkedList<>();
        for (Statement statement : statements) {
            if (filter.test(statement)) {
                result.add(statement);
            }
        }
        return new StatementsCollection(result);
    }

    public Collection<Value> values(Resource subject, IRI predicate) {
        final Set<Value> result = new HashSet<>();
        for (Statement s : statements) {
            if ((subject == null || s.getSubject().equals(subject)) &&
                    (predicate == null || s.getPredicate().equals(predicate))) {
                result.add(s.getObject());
            }
        }
        return result;
    }

    public Value value(Resource subject, IRI predicate) {
        final Collection<Value> values = values(subject, predicate);
        if (values.size() == 1) {
            return values.iterator().next();
        } else {
            throw new RuntimeException("Missing value.");
        }
    }

    public Collection<Statement> getStatements() {
        return Collections.unmodifiableCollection(statements);
    }

    /**
     * @param toRemove Statements to removeProperties.
     */
    public void remove(Collection<Statement> toRemove) {
        statements.removeAll(toRemove);
    }

    /**
     * @param toRemove Statements to removeProperties.
     */
    public void remove(StatementsCollection toRemove) {
        remove(toRemove.statements);
    }

    public int size() {
        return statements.size();
    }

}
