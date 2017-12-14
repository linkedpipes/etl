package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.rdf4j.driver.VirtuosoRepository;

import java.sql.SQLException;

public final class Virtuoso implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Virtuoso.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public VirtuosoConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

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
        prepareSqlExecutor();
        clearLoadList();
        fillLoadList();
        int filesToLoadCount = getFilesToLoadCount();
        if (filesToLoadCount == 0) {
            throw exceptionFactory.failure("Nothing to load.");
        }
        if (configuration.isClearDestinationGraph()) {
            clearDestinationGraph();
        }
        progressReport.start(filesToLoadCount);
        runLoaders(filesToLoadCount);
        progressReport.done();
        clearLoadList();
    }

    private void prepareSqlExecutor() throws LpException {
        try {
            new virtuoso.jdbc4.Driver();
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't create SQL driver.", ex);
        }
        sqlExecutor = new SqlExecutor(
                configuration.getVirtuosoUrl(),
                configuration.getUsername(),
                configuration.getPassword(),
                configuration.getLoadDirectoryPath(),
                exceptionFactory);
    }

    private void fillLoadList() throws LpException {
        sqlExecutor.insertLoadRecord(
                configuration.getLoadFileName(),
                configuration.getTargetGraph());
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

    private int getFilesToLoadCount() throws LpException {
        final int filesToLoad = sqlExecutor.getFilesToLoad();
        LOG.info("Files to load: {}", filesToLoad);
        return filesToLoad;
    }

    private void runLoaders(int filesToLoad) throws LpException {
        MultiThreadLoader loader = new MultiThreadLoader(
                sqlExecutor, configuration, exceptionFactory, progressReport);
        loader.loadData(filesToLoad);
    }

    private void clearLoadList() throws LpException {
        sqlExecutor.clearLoadList();
    }

}
