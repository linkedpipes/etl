package com.linkedpipes.etl.test;

import com.linkedpipes.etl.dataunit.core.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestGraphListDataUnit
        implements GraphListDataUnit, WritableGraphListDataUnit {

    private final List<IRI> graphs = new ArrayList<>(4);

    private final String baseIri;

    private final Repository repository;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public TestGraphListDataUnit(String baseIri,
            Repository repository) {
        this.baseIri = baseIri;
        this.repository = repository;
    }

    @Override
    public IRI createGraph() throws LpException {
        final IRI iri = valueFactory.createIRI(baseIri + "/" + graphs.size());
        graphs.add(iri);
        return iri;
    }

    @Override
    public Collection<IRI> getReadGraphs() throws LpException {
        return graphs;
    }

    @Override
    public void execute(RepositoryProcedure action)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            action.accept(connection);
        }
    }

    @Override
    public <T> T execute(RepositoryFunction<T> action)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            return action.accept(connection);
        }
    }

    @Override
    public void execute(Procedure action) throws LpException {
        action.accept();
    }

    @Override
    public Repository getRepository() {
        return repository;
    }
}
