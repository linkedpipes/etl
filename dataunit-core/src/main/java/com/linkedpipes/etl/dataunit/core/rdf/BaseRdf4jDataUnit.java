package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;

import java.util.Collection;

/**
 * Base class for RDF4J data units based on RDF4J library.
 */
abstract class BaseRdf4jDataUnit implements Rdf4jDataUnit, ManageableDataUnit {

    protected final static ValueFactory VF = SimpleValueFactory.getInstance();

    private final String binding;

    private final String iri;

    protected final Repository repository;

    protected final Collection<String> sources;

    public BaseRdf4jDataUnit(String binding, String iri,
            Repository repository, Collection<String> sources) {
        this.binding = binding;
        this.iri = iri;
        this.repository = repository;
        this.sources = sources;
    }

    @Override
    public void execute(RepositoryProcedure action) throws LpException {
        ActionExecutor.execute(repository, action);
    }

    @Override
    public <T> T execute(RepositoryFunction<T> action) throws LpException {
        return ActionExecutor.execute(repository, action);
    }

    @Override
    public void execute(Procedure action) throws LpException {
        ActionExecutor.execute(action);
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    public String getIri() {
        return iri;
    }

}

