package com.linkedpipes.etl.test.dataunit;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class TestSingleGraphDataUnit
        implements SingleGraphDataUnit, WritableSingleGraphDataUnit {

    private final IRI graph;

    private final Repository repository;

    public TestSingleGraphDataUnit(IRI graph,
            Repository repository) {
        this.graph = graph;
        this.repository = repository;
    }

    @Override
    public IRI getWriteGraph() {
        return graph;
    }

    @Override
    public IRI getReadGraph() {
        return graph;
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
