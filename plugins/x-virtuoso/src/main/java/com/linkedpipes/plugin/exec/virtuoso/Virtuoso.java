package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
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

/**
 *
 * @author Škoda Petr
 */
public final class Virtuoso implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Virtuoso.class);

    private static final String SQL_LD_DIR = "ld_dir (?, ?, ?)";

    private static final String SQL_QUERY_WAITING
            = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state <> 2";

    private static final String SQL_DELETE_LOAD_LIST
            = "delete from DB.DBA.load_list where ll_file like ?";

    private static final String SQL_QUERY_FINISHED
            = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 2";

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @DataProcessingUnit.Configuration
    public VirtuosoConfiguration configuration;

    @DataProcessingUnit.Extension
    public FaultTolerance faultTolerance;

    @DataProcessingUnit.Extension
    public AfterExecution cleanUp;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        // Create remote repository.
        final VirtuosoRepository virtuosoRepository = new VirtuosoRepository(
                configuration.getVirtuosoUrl(), configuration.getUsername(), configuration.getPassword());
        try {
            virtuosoRepository.initialize();
        } catch (RepositoryException ex) {
            throw new DataProcessingUnit.ExecutionFailed(ex, "Can't connect to Virtuoso repository.");
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
            faultTolerance.call(() -> {
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
            });
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
            throw new DataProcessingUnit.ExecutionFailed(ex, "Can't execute ld_dir query.");
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
            throw new DataProcessingUnit.ExecutionFailed(ex, "Can't query load_list table.");
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
        while (!context.canceled()) {
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

    private Connection getSqlConnection() throws DataProcessingUnit.ExecutionFailed {
        try {
            return DriverManager.getConnection(configuration.getVirtuosoUrl(), configuration.getUsername(),
                    configuration.getPassword());
        } catch (SQLException ex) {
            throw new DataProcessingUnit.ExecutionFailed(ex, "Can't create sql connection.");
        }
    }

    private void startLoading() throws DataProcessingUnit.ExecutionFailed {
        // Start loading.
        Connection loaderConnection = null;
        try {
            loaderConnection = getSqlConnection();
            try (Statement statementRun = loaderConnection.createStatement()) {
                final ResultSet resultSetRun = statementRun.executeQuery("rdf_loader_run()");
                resultSetRun.close();
            }
        } catch (SQLException ex) {
            throw new DataProcessingUnit.ExecutionFailed(ex, "Can't start loading.");
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
