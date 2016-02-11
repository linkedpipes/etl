package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import java.util.Collection;

import org.openrdf.repository.Repository;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import java.io.File;

/**
 *
 * @author Škoda Petr
 */
abstract class SesameDataUnitImpl implements SesameDataUnit, ManagableDataUnit {

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

    /**
     * If not null then into this directory debug data should be saved in {@link #close()} method.
     */
    protected final File debugDirectory;

    protected SesameDataUnitImpl(Repository repository, RdfDataUnitConfiguration configuration) {
        this.id = configuration.getBinding();
        this.resourceUri = configuration.getResourceUri();
        this.repository = repository;
        this.sources = configuration.getSourceDataUnitUris();
        this.debugDirectory = configuration.getDebugDirectory();
    }

    @Override
    public String getBinding() {
        return id;
    }

    @Override
    public String getResourceUri() {
        return resourceUri;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void execute(RepositoryProcedure action) throws RepositoryActionFailed {
        ActionExecutor.execute(repository, action);
    }

    @Override
    public <T> T execute(RepositoryFunction<T> action) throws RepositoryActionFailed {
        return ActionExecutor.execute(repository, action);
    }

    @Override
    public void execute(Procedure action) throws RepositoryActionFailed {
        ActionExecutor.execute(repository, action);
    }

    /**
     * If possible utilize {@link #execute(cz.cuni.mff.xrg.cuv.dataunit.sesame.boundary.RepositoryAction)} rather then
     * direct access to repository.
     *
     * @return
     */
    @Override
    public Repository getRepository() {
        return repository;
    }

}
