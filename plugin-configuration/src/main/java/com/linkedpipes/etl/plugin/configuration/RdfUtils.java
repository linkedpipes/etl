package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

class RdfUtils {

    public static void updateSubject(
            List<Statement> statements, Resource source, Resource target) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<Statement> toAdd = new ArrayList<>();
        List<Statement> toRemove = new ArrayList<>();
        for (Statement statement : statements) {
            if (statement.getSubject().equals(source)) {
                toAdd.add(valueFactory.createStatement(
                        target,
                        statement.getPredicate(),
                        statement.getObject(),
                        statement.getContext()));
                toRemove.add(statement);
            }
        }
        statements.removeAll(toRemove);
        statements.addAll(toAdd);
    }

    public static List<Statement> setGraph(
            List<Statement> statements, IRI graph) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<Statement> result = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            result.add(valueFactory.createStatement(
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject(),
                    graph
            ));
        }
        return result;
    }

}
