package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.Repositories;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author Petr Škoda
 */
class RdfDataSource implements SparqlSelect {

    protected Repository repository;

    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();

    RdfDataSource() {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();
    }

    void close() {
        repository.shutDown();
    }

    void add(Statement statement) {
        Repositories.consume(repository, (connection) -> {
            connection.add(statement);
        });
    }

    void add(String subject, IRI predicate, Value value) {
        Repositories.consume(repository, (connection) -> {
            connection.add(valueFactory.createStatement(
                    valueFactory.createIRI(subject),
                    predicate, value));
        });
    }

    void add(String subject, String predicate, Value value) {
        Repositories.consume(repository, (connection) -> {
            connection.add(valueFactory.createStatement(
                    valueFactory.createIRI(subject),
                    valueFactory.createIRI(predicate),
                    value));
        });
    }

    @Override
    public List<Map<String, String>> executeSelect(String query) throws RdfException {
        return Repositories.get(repository, (connection) -> {
            List<Map<String, String>> output = new LinkedList<>();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, query);
            final TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                final Map<String, String> record = new HashMap<>();
                for (Binding item : binding) {
                    record.put(item.getName(),
                            item.getValue().stringValue());
                }
                output.add(record);
            }
            return output;
        });
    }

}
