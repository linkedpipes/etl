package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.AbstractDataUnit;
import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for RDF4J data units based on RDF4J library.
 */
abstract class BaseRdf4jDataUnit extends AbstractDataUnit
        implements Rdf4jDataUnit, ManageableDataUnit {

    protected static final ValueFactory VF = SimpleValueFactory.getInstance();

    protected final RepositoryManager repositoryManager;

    private Repository repository;

    public BaseRdf4jDataUnit(
            DataUnitConfiguration configuration,
            Collection<String> sources,
            RepositoryManager manager) {
        super(configuration, sources);
        this.repositoryManager = manager;
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
    public void initialize(File directory) throws LpException {
        setRepositoryFromManager();
    }

    @Override
    public void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException {
        setRepositoryFromManager();
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    protected void setRepositoryFromManager() throws LpException {
        if (this.repository == null) {
            this.repository = this.repositoryManager.getRepository(
                    this.configuration);
        } else {
            throw new LpException("Repository has already been initialized.");
        }
    }

}

