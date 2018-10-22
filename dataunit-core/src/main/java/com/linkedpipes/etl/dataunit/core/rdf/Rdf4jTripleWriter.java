package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Rdf4jTripleWriter implements TripleWriter {

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final IRI graph;

    private final Rdf4jDataUnit dataUnit;

    public Rdf4jTripleWriter(IRI graph, Rdf4jDataUnit dataUnit) {
        this.graph = graph;
        this.dataUnit = dataUnit;
    }

    @Override
    public void iri(String subject, String predicate, String object) {
        add(subject, predicate, this.valueFactory.createIRI(object));
    }

    private void add(String subject, String predicate, Value value) {
        statements.add(this.valueFactory.createStatement(
                this.valueFactory.createIRI(subject),
                this.valueFactory.createIRI(predicate),
                value
        ));
    }

    @Override
    public void string(String subject, String predicate, String object) {
        add(subject, predicate, this.valueFactory.createLiteral(object));
    }

    @Override
    public void string(String subject, String predicate, String object,
                       String lang) {
        add(subject, predicate, this.valueFactory.createLiteral(object, lang));
    }

    @Override
    public void date(String subject, String predicate, Date object) {
        add(subject, predicate, this.valueFactory.createLiteral(object));
    }

    @Override
    public void typed(String subject, String predicate, String object,
                      String type) {
        Value value = this.valueFactory.createLiteral(
                object, this.valueFactory.createIRI(type));
        add(subject, predicate, value);
    }

    @Override
    public synchronized void flush() throws RdfException {
        try {
            this.dataUnit.execute(connection -> {
                connection.add(this.statements, this.graph);
            });
        } catch (LpException ex) {
            throw new RdfException("Can't store data.", ex);
        }
        this.statements.clear();
    }

}
