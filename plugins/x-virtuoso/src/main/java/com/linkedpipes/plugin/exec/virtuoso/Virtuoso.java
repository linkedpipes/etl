package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.AfterExecution;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.sesame2.driver.VirtuosoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Virtuoso implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(Virtuoso.class);

    private static final String SQL_LD_DIR = "ld_dir (?, ?, ?)";

    private static final String SQL_QUERY_WAITING
            =
            "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state <> 2";

    private static final String SQL_DELETE_LOAD_LIST
            = "delete from DB.DBA.load_list where ll_file like ?";

    private static final String SQL_QUERY_FINISHED
            =
            "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 2";

    @Component.Configuration
    public VirtuosoConfiguration configuration;

    @Component.Inject
    public AfterExecution cleanUp;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        // Create remote repository.
        final VirtuosoRepository virtuosoRepository = new VirtuosoRepository(
                configuration.getVirtuosoUrl(), configuration.getUsername(),
                configuration.getPassword());
        try {
            virtuosoRepository.initialize();
        } catch (RepositoryException ex) {
            throw exceptionFactory
                    .failure("Can't connect to Virtuoso repository.", ex);
        }
        cleanUp.addAction(() -> {
            try {
                virtuosoRepository.shutDown();
            } catch (RepositoryException ex) {
                LOG.warn("Can't close repository.", ex);
            }
        });
        // Delete data if set.
        if (configuration.isClearDestinationGraph()) {
            RepositoryConnection repositoryConnection =
                    virtuosoRepository.getConnection();
            try {
                final Update update = repositoryConnection.prepareUpdate(
                        QueryLanguage.SPARQL,
                        getClearQuery(configuration.getTargetGraph()));
                update.execute();
            } finally {
                try {
                    repositoryConnection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
        final Connection connectionForInit = getSqlConnection();
        // Insert data to load table.
        try (PreparedStatement statementLdDir = connectionForInit
                .prepareStatement(SQL_LD_DIR)) {
            statementLdDir.setString(1, configuration.getLoadDirectoryPath());
            statementLdDir.setString(2, configuration.getLoadFileName());
            statementLdDir.setString(3, configuration.getTargetGraph());
            // Execute.
            final ResultSet resultSetLdDir = statementLdDir.executeQuery();
            resultSetLdDir.close();
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't execute ld_dir query.", ex);
        }
        // Check number of files to load.
        final int filesToLoad;
        try (PreparedStatement statementStatusCountProcessing = connectionForInit
                .prepareStatement(SQL_QUERY_WAITING)) {
            statementStatusCountProcessing
                    .setString(1, configuration.getLoadDirectoryPath() + "%");
            try (ResultSet resultSetProcessing = statementStatusCountProcessing
                    .executeQuery()) {
                resultSetProcessing.next();
                filesToLoad = resultSetProcessing.getInt(1);
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't query load_list table.", ex);
        } finally {
            // Here we can close the connection.
            try {
                connectionForInit.close();
            } catch (SQLException ex) {
                LOG.warn("Can't close SQL connection.", ex);
            }
        }
        LOG.info("Load list holds {} files to process", filesToLoad);
        if (filesToLoad == 0) {
            LOG.info("Nothing to do. Stopping.");
            return;
        }
        // Start loading.
        int loaders = configuration.getLoaderCount() > 1 ?
                configuration.getLoaderCount() : 1;
        final ExecutorService executor = Executors.newFixedThreadPool(loaders);
        final List<LoadWorker> workers = new ArrayList<>(loaders);
        for (int i = 0; i < loaders; ++i) {
            final LoadWorker worker =
                    new LoadWorker(configuration, exceptionFactory);
            executor.submit(worker);
            workers.add(worker);
        }
        while (true) {
            // Check workers.
            try {
                if (executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
            // Check for status.
            final Connection connectionForStatusCheck = getSqlConnection();
            try (PreparedStatement statement = connectionForStatusCheck
                    .prepareStatement(SQL_QUERY_FINISHED)) {
                statement.setString(1,
                        configuration.getLoadDirectoryPath() + "%");
                try (ResultSet resultSetDoneLoop = statement.executeQuery()) {
                    resultSetDoneLoop.next();
                    int filesLoaded = resultSetDoneLoop.getInt(1);
                    LOG.info("Processing {}/{} files", filesLoaded,
                            filesToLoad);
                    if (filesLoaded == filesToLoad) {
                        break;
                    }
                }
            } catch (SQLException ex) {
                LOG.warn("Can't query status.", ex);
            } finally {
                try {
                    connectionForStatusCheck.close();
                } catch (SQLException ex) {
                    LOG.warn("Can't close SQL connection.", ex);
                }
            }
            // Wait before next check.
            try {
                Thread.sleep(configuration.getStatusUpdateInterval() * 1000);
            } catch (InterruptedException ex) {
                // Do nothing here.
            }
        }
        // Wait for shutdown.
        LOG.warn("Awaiting termination ...");
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
        LOG.warn("Awaiting termination ... done");
        //
        if (configuration.isClearLoadList()) {
            final Connection connectionForDelete = getSqlConnection();
            // Delete from loading table - made optional.
            try (PreparedStatement delete = connectionForDelete
                    .prepareStatement(SQL_DELETE_LOAD_LIST)) {
                delete.setString(1, configuration.getLoadDirectoryPath() + "%");
                delete.executeUpdate();
            } catch (SQLException ex) {

            } finally {
                try {
                    connectionForDelete.close();
                } catch (SQLException ex) {
                    LOG.warn("Can't close SQL connection.", ex);
                }
            }
        }
        // Check results.
        for (LoadWorker worker : workers) {
            if (worker.isFailed()) {
                throw exceptionFactory.failure(
                        "Can't load data. See logs for more exception.");
            }
        }
    }

    private String getClearQuery(String graph) {
        return "DEFINE sql:log-enable 3 CLEAR GRAPH <" + graph + ">";
    }

    private Connection getSqlConnection() throws LpException {
        try {
            return DriverManager.getConnection(configuration.getVirtuosoUrl(),
                    configuration.getUsername(),
                    configuration.getPassword());
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't create sql connection.", ex);
        }
    }

}
