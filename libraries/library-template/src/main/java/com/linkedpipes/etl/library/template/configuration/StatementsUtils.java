package com.linkedpipes.etl.library.template.configuration;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

class StatementsUtils {

    public static void renameSubject(
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

}
