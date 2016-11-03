package com.linkedpipes.etl.storage.rdf;

import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.*;
import java.util.function.Function;

public class StatementsCollection {

    private final Collection<Statement> statements;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

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

    public StatementsCollection filter(Function<Statement, Boolean> filter) {
        final List<Statement> result = new LinkedList<>();
        for (Statement statement : statements) {
            if (filter.apply(statement)) {
                result.add(statement);
            }
        }
        return new StatementsCollection(result);
    }

    public Collection<Resource> subjects(IRI predicate, Value object) {
        final Set<Resource> result = new HashSet<>();
        for (Statement s : statements) {
            if ((predicate == null || s.getPredicate().equals(predicate)) &&
                    (object == null || s.getObject().equals(object))) {
                result.add(s.getSubject());
            }
        }
        return result;
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

    /**
     * Replace one resource with another one.
     *
     * @param oldValue
     * @param newValue
     */
    public void replace(Resource oldValue, Resource newValue) {
        final Collection<Statement> toRemove = new LinkedList<>();
        final Collection<Statement> toAdd = new LinkedList<>();
        for (Statement s : statements) {
            if (s.getSubject().equals(oldValue)) {
                if (s.getObject().equals(oldValue)) {
                    toRemove.add(s);
                    toRemove.add(s);
                    toAdd.add(valueFactory.createStatement(
                            newValue, s.getPredicate(),
                            newValue, s.getContext()
                    ));
                } else {
                    toRemove.add(s);
                    toRemove.add(s);
                    toAdd.add(valueFactory.createStatement(
                            newValue, s.getPredicate(),
                            s.getObject(), s.getContext()
                    ));
                }
            } else {
                if (s.getObject().equals(oldValue)) {
                    toRemove.add(s);
                    toAdd.add(valueFactory.createStatement(
                            s.getSubject(), s.getPredicate(),
                            newValue, s.getContext()
                    ));
                } else {
                    // No match.
                }
            }
        }
        statements.removeAll(toRemove);
        statements.addAll(toAdd);
    }

    public int size() {
        return statements.size();
    }

}
