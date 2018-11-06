package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

// TODO Replace with Statements.
public class StatementsCollection {

    private final Collection<Statement> statements;

    /**
     * Operate on given collection.
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
        List<Statement> result = new LinkedList<>();
        for (Statement statement : statements) {
            if (filter.test(statement)) {
                result.add(statement);
            }
        }
        return new StatementsCollection(result);
    }

    public Collection<Value> values(Resource subject, IRI predicate) {
        Set<Value> result = new HashSet<>();
        for (Statement s : statements) {
            boolean subjectMatch =
                    (subject == null || s.getSubject().equals(subject));
            boolean predicateMatch =
                    (predicate == null || s.getPredicate().equals(predicate));
            if (subjectMatch && predicateMatch) {
                result.add(s.getObject());
            }
        }
        return result;
    }

    public Value value(Resource subject, IRI predicate) {
        Collection<Value> values = values(subject, predicate);
        if (values.size() == 1) {
            return values.iterator().next();
        } else {
            throw new RuntimeException("Missing value.");
        }
    }

    public Collection<Statement> getStatements() {
        return Collections.unmodifiableCollection(statements);
    }

    public void remove(Collection<Statement> toRemove) {
        statements.removeAll(toRemove);
    }

    public void remove(StatementsCollection toRemove) {
        remove(toRemove.statements);
    }

    public int size() {
        return statements.size();
    }

}
