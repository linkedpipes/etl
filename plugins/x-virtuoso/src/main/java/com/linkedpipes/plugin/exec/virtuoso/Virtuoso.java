package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.rdf4j.driver.VirtuosoRepository;

public final class Virtuoso implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Virtuoso.class);

    @Component.Configuration
    public VirtuosoConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private Repository repository;

    private SqlExecutor sqlExecutor;

    @Override
    public void execute() throws LpException {
        createRepository();
        try {
            loadToRepository();
        } finally {
            closeRepository();
        }
    }

    private void createRepository() throws LpException {
        repository = new VirtuosoRepository(
                configuration.getVirtuosoUrl(),
                configuration.getUsername(),
                configuration.getPassword());
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw exceptionFactory.failure(
                    "Can't connect to Virtuoso repository.", ex);
        }
    }

    private void closeRepository() {
        try {
            repository.shutDown();
        } catch (RepositoryException ex) {
            LOG.warn("Can't close repository.", ex);
        }
    }

    private void loadToRepository() throws LpException {
        if (configuration.isClearDestinationGraph()) {
            clearDestinationGraph();
        }
        prepareSqlExecutor();
        sqlExecutor.insertLoadRecord(
                configuration.getLoadDirectoryPath(),
                configuration.getLoadFileName(),
                configuration.getTargetGraph());
        final int filesToLoad = sqlExecutor.getFilesToLoad();
        LOG.info("Files to load: {}", filesToLoad);
        if (filesToLoad == 0) {
            return;
        }
        final MultiThreadLoader loader = new MultiThreadLoader(
                sqlExecutor, configuration, exceptionFactory);
        loader.loadData(filesToLoad);
        if (configuration.isClearLoadList()) {
            sqlExecutor.clearLoadList(configuration.getLoadDirectoryPath());
        }
    }

    private void prepareSqlExecutor() {
        sqlExecutor = new SqlExecutor(
                configuration.getVirtuosoUrl(),
                configuration.getUsername(),
                configuration.getPassword(),
                exceptionFactory);
    }

    private void clearDestinationGraph() throws LpException {
        LOG.debug("clearDestinationGraph ... ");
        final RepositoryConnection connection = repository.getConnection();
        try {
            final Update update = connection.prepareUpdate(
                    QueryLanguage.SPARQL,
                    getClearQuery());
            update.execute();
        } finally {
            LOG.debug("clearDestinationGraph ... done");
            try {
                connection.close();
            } catch (RepositoryException ex) {
                LOG.warn("Can't close connection.", ex);
            }
        }
    }

    private String getClearQuery() {
        return "DEFINE sql:log-enable 3 CLEAR GRAPH <" +
                configuration.getTargetGraph() +
                ">";
    }

}
