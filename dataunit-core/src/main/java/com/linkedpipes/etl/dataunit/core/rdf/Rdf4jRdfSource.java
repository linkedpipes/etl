package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * We use graph provided in the constructor.
 */
class Rdf4jRdfSource implements RdfSource {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final BaseRdf4jDataUnit dataUnit;

    private final IRI graph;

    public Rdf4jRdfSource(BaseRdf4jDataUnit dataUnit, IRI graph) {
        this.dataUnit = dataUnit;
        this.graph = graph;
    }

    @Override
    public void statements(String subject, StatementHandler handler)
            throws RdfException {
        IRI iri = valueFactory.createIRI(subject);
        try {
            dataUnit.execute((connection -> {
                RepositoryResult<Statement> result = connection.getStatements(
                        iri, null, null, this.graph);
                handleResult(result, handler);
            }));
        } catch (LpException ex) {
            throw new RdfException("Can't iterate statements", ex);
        }
    }

    private void handleResult(
            RepositoryResult<Statement> result,
            StatementHandler handler) throws RdfException {
        while (result.hasNext()) {
            Statement statement = result.next();
            handler.accept(statement.getPredicate().stringValue(),
                    new Rdf4jValueWrap(statement.getObject())
            );
        }
    }

    @Override
    public List<RdfValue> getPropertyValues(String subject, String predicate)
            throws RdfException {
        IRI s = this.valueFactory.createIRI(subject);
        IRI p = this.valueFactory.createIRI(predicate);
        List<RdfValue> result = new ArrayList<>();
        try {
            this.dataUnit.execute((connection -> {
                RepositoryResult<Statement> statements =
                        connection.getStatements(s, p, null, this.graph);
                while (statements.hasNext()) {
                    result.add(
                            new Rdf4jValueWrap(statements.next().getObject()));
                }
            }));
        } catch (LpException ex) {
            throw new RdfException("Can't iterate statements", ex);
        }
        return result;
    }

    @Override
    public List<String> getByType(String type) throws RdfException {
        IRI typeIri = this.valueFactory.createIRI(type);
        List<String> result = new ArrayList<>();
        try {
            this.dataUnit.execute((connection -> {
                RepositoryResult<Statement> statements =
                        connection.getStatements(
                                null, RDF.TYPE, typeIri, this.graph);
                while (statements.hasNext()) {
                    result.add(statements.next().getSubject().stringValue());
                }
            }));
        } catch (LpException ex) {
            throw new RdfException("Can't iterate statements", ex);
        }
        return result;
    }

}
