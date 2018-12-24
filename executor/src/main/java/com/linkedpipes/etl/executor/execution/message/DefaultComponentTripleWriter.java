package com.linkedpipes.etl.executor.execution.message;

import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.RdfFormatter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;
import java.util.Date;

class DefaultComponentTripleWriter implements TripleWriter {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Collection<Statement> statements;

    private final IRI graph;

    private final RdfFormatter format = new RdfFormatter();

    public DefaultComponentTripleWriter(
            Collection<Statement> statements, IRI graph) {
        this.statements = statements;
        this.graph = graph;
    }

    @Override
    public void iri(String s, String p, String o) {
        this.statements.add(this.valueFactory.createStatement(
                this.valueFactory.createIRI(s),
                this.valueFactory.createIRI(p),
                this.valueFactory.createIRI(o),
                this.graph));
    }

    @Override
    public void string(String subject, String predicate, String object) {
        this.statements.add(this.valueFactory.createStatement(
                this.valueFactory.createIRI(subject),
                this.valueFactory.createIRI(predicate),
                this.valueFactory.createLiteral(object),
                this.graph));
    }

    @Override
    public void string(String s, String p, String o, String language) {
        if (language == null) {
            this.string(s, p, o);
        } else {
            this.statements.add(this.valueFactory.createStatement(
                    this.valueFactory.createIRI(s),
                    this.valueFactory.createIRI(p),
                    this.valueFactory.createLiteral(o, language),
                    this.graph));
        }
    }

    @Override
    public void typed(String s, String p, String o, String type) {
        this.statements.add(this.valueFactory.createStatement(
                this.valueFactory.createIRI(s),
                this.valueFactory.createIRI(p),
                this.valueFactory.createLiteral(
                        o, this.valueFactory.createIRI(type)),
                this.graph));
    }

    @Override
    public void date(String subject, String predicate, Date object) {
        String value = format.toXsdDate(object);
        typed(subject, predicate, value, XSD.DATETIME);
    }

    @Override
    public void flush() {
        // Do nothing here.
    }

}
