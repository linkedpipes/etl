package com.linkedpipes.plugin.exec.virtuoso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.sesame2.driver.VirtuosoRepository;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.AfterExecution;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class Virtuoso implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(Virtuoso.class);

    private static final String SQL_LD_DIR = "ld_dir (?, ?, ?)";

    private static final String SQL_QUERY_WAITING
            = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state <> 2";

    private static final String SQL_DELETE_LOAD_LIST
            = "delete from DB.DBA.load_list where ll_file like ?";

    private static final String SQL_QUERY_FINISHED
            = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 2";

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
                configuration.getVirtuosoUrl(), configuration.getUsername(), configuration.getPassword());
        try {
            virtuosoRepository.initialize();
        } catch (RepositoryException ex) {
            throw exceptionFactory.failure("Can't connect to Virtuoso repository.", ex);
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
            RepositoryConnection repositoryConnection = virtuosoRepository.getConnection();
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
        try (PreparedStatement statementLdDir = connectionForInit.prepareStatement(SQL_LD_DIR)) {
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
        try (PreparedStatement statementStatusCountProcessing = connectionForInit.prepareStatement(SQL_QUERY_WAITING)) {
            statementStatusCountProcessing.setString(1, configuration.getLoadDirectoryPath() + "%");
            try (ResultSet resultSetProcessing = statementStatusCountProcessing.executeQuery()) {
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
        // Start loading, we use only a single thread call here.
        startLoading();
        // Check for status - periodic and final.
        while (true) {
            final Connection connectionForStatusCheck = getSqlConnection();
            try (PreparedStatement statement = connectionForStatusCheck.prepareStatement(SQL_QUERY_FINISHED)) {
                statement.setString(1, configuration.getLoadDirectoryPath() + "%");
                try (ResultSet resultSetDoneLoop = statement.executeQuery()) {
                    resultSetDoneLoop.next();
                    int filesLoaded = resultSetDoneLoop.getInt(1);
                    LOG.info("Processing {}/{} files", filesLoaded, filesToLoad);
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
        if (configuration.isClearLoadList()) {
            final Connection connectionForDelete = getSqlConnection();
            // Delete from loading table - made optional.
            try (PreparedStatement delete = connectionForDelete.prepareStatement(SQL_DELETE_LOAD_LIST)) {
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
    }

    private String getClearQuery(String graph) {
        return "DEFINE sql:log-enable 3 CLEAR GRAPH <" + graph + ">";
    }

    private Connection getSqlConnection() throws LpException {
        try {
            return DriverManager.getConnection(configuration.getVirtuosoUrl(), configuration.getUsername(),
                    configuration.getPassword());
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't create sql connection.", ex);
        }
    }

    private void startLoading() throws LpException {
        // Start loading.
        Connection loaderConnection = null;
        try {
            loaderConnection = getSqlConnection();
            try (Statement statementRun = loaderConnection.createStatement()) {
                final ResultSet resultSetRun = statementRun.executeQuery("rdf_loader_run()");
                resultSetRun.close();
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't start loading.", ex);
        } finally {
            try {
                if (loaderConnection != null) {
                    loaderConnection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Can't close SQL connection.", ex);
            }
        }
    }

}
