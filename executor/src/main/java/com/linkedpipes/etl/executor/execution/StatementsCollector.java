package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.rdf.SerializableToRdf;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

class StatementsCollector implements SerializableToRdf.Writer {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final List<Statement> statements = new ArrayList<>(8);

    private final IRI graph;

    StatementsCollector(IRI graph) {
        this.graph = graph;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void add(String subject, String predicate, String object,
            String type) {
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createLiteral(object,
                        valueFactory.createIRI(type)), graph));
    }

    @Override
    public void addUri(String subject, String predicate, String object) {
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createIRI(object), graph));
    }

    @Override
    public void addString(String subject, String predicate, String object,
            String language) {
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createLiteral(object, language), graph));
    }

}
