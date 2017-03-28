package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.BaseDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;

import java.util.Collection;

/**
 * Base class for RDF4J data units based on RDF4J library.
 */
abstract class BaseRdf4jDataUnit
        extends BaseDataUnit
        implements Rdf4jDataUnit, ManageableDataUnit {

    protected final static ValueFactory VF = SimpleValueFactory.getInstance();

    protected final Repository repository;

    public BaseRdf4jDataUnit(String binding, String iri,
            Repository repository, Collection<String> sources) {
        super(binding, iri, sources);
        this.repository = repository;
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


}

