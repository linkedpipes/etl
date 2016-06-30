package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import java.util.Collection;

import org.openrdf.repository.Repository;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
abstract class SesameDataUnitImpl implements SesameDataUnit, ManagableDataUnit {

    /**
     * Binding.
     */
    private final String id;

    private final String resourceUri;

    /**
     * Determine if this data unit was initialized or not.
     */
    protected boolean initialized = false;

    /**
     * Utilized repository.
     */
    protected final Repository repository;

    /**
     * List of source data unit URIs.
     */
    protected final Collection<String> sources;

    protected SesameDataUnitImpl(Repository repository,
            RdfDataUnitConfiguration configuration) {
        this.id = configuration.getBinding();
        this.resourceUri = configuration.getResourceIri();
        this.repository = repository;
        this.sources = configuration.getSourceDataUnitIris();
    }

    @Override
    public String getBinding() {
        return id;
    }

    @Override
    public String getResourceIri() {
        return resourceUri;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
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
        ActionExecutor.execute(repository, action);
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

}
