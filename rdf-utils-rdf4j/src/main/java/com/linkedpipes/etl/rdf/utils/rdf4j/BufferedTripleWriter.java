package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfTriple;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add support for RdfValue implemented by Rdf4j.
 * TODO Add support for RdfTriple implemented by Rdf4j.
 */
class BufferedTripleWriter implements BackendTripleWriter {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private final List<Statement> buffer = new ArrayList<>();

    private final String graph;

    private final Repository repository;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public BufferedTripleWriter(String graph, Repository repository) {
        this.graph = graph;
        this.repository = repository;
    }

    @Override
    public void iri(String subject, String predicate, String object) {
        buffer.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createIRI(object)
        ));
    }

    @Override
    public void bool(String subject, String predicate, boolean object) {
        buffer.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createLiteral(object)
        ));
    }

    @Override
    public void string(
            String subject, String predicate, String object, String language) {
        Value value;
        if (language == null) {
            value = valueFactory.createLiteral(object);
        } else {
            value = valueFactory.createLiteral(object, language);
        }
        buffer.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                value
        ));
    }

    @Override
    public void typed(
            String subject, String predicate, String object, String type) {
        buffer.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                valueFactory.createLiteral(object,
                        valueFactory.createIRI(type))
        ));
    }

    @Override
    public void add(String subject, String predicate, BackendRdfValue value) {
        Value rdf4jValue = asRdf4jValue(value);
        buffer.add(valueFactory.createStatement(
                valueFactory.createIRI(subject),
                valueFactory.createIRI(predicate),
                rdf4jValue
        ));
    }

    @Override
    public void add(RdfTriple triple) {
        add(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    private Value asRdf4jValue(BackendRdfValue value) {
        if (value.isIri()) {
            return valueFactory.createIRI(value.asString());
        }
        String type = value.getType();
        if (type.equals(XSD.STRING)) {
            return valueFactory.createLiteral(value.asString());
        }
        if (type.equals(XSD.LANG_STRING)) {
            return valueFactory.createLiteral(value.asString(),
                    value.getLanguage());
        }
        return valueFactory.createLiteral(value.asString(),
                valueFactory.createIRI(type));
    }

    @Override
    public void flush() throws RdfUtilsException {
        try {
            Repositories.consume(repository, (connection) -> {
                connection.add(buffer, VF.createIRI(graph));
            });
            buffer.clear();
        } catch (RuntimeException ex) {
            throw new RdfUtilsException("Can't add triples to repository.");
        }
    }

}
