package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfTriple;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.LinkedList;
import java.util.List;

public class StatementsCollector implements TripleWriter {

    private List<Statement> statements = new LinkedList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final IRI graph;

    public StatementsCollector(String graph) {
        this.graph = valueFactory.createIRI(graph);
    }

    private void add(String subject, String predicate, Value object) {
        if (graph == null) {
            statements.add(valueFactory.createStatement(
                    valueFactory.createIRI(subject),
                    valueFactory.createIRI(predicate),
                    object));
        } else {
            statements.add(valueFactory.createStatement(
                    valueFactory.createIRI(subject),
                    valueFactory.createIRI(predicate),
                    object, graph));
        }
    }

    public void add(Statement statement) {
        statements.add(statement);
    }

    @Override
    public void iri(String subject, String predicate, String object) {
        add(subject, predicate, valueFactory.createIRI(object));
    }

    @Override
    public void bool(String subject, String predicate, boolean object) {
        add(subject, predicate, valueFactory.createLiteral(object));
    }

    @Override
    public void string(String subject, String predicate, String object,
            String language) {
        Value objectValue;
        if (language == null) {
            objectValue = valueFactory.createLiteral(object);
        } else {
            objectValue = valueFactory.createLiteral(object, language);
        }
        add(subject, predicate, objectValue);
    }

    @Override
    public void typed(String subject, String predicate, String object,
            String type) {
        Value value = valueFactory.createLiteral(
                object, valueFactory.createIRI(type));
        add(subject, predicate, value);
    }

    @Override
    public void add(String subject, String predicate, RdfValue value) {
        if(value.getType() == null) {
            iri(subject, predicate, value.asString());
        } else if (value.getType().equals(XSD.LANG_STRING)) {
            string(subject, predicate, value.asString(), value.getLanguage());
        } else {
            typed(subject, predicate, value.asString(), value.getType());
        }
    }

    @Override
    public void add(RdfTriple triple) {
        add(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
    public void flush() throws RdfUtilsException {
        // No operation here.
    }

    public List<Statement> getStatements() {
        return statements;
    }

}
