package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.Callable;

class LoadWorker implements Callable<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(LoadWorker.class);

    private final VirtuosoConfiguration configuration;

    private ExceptionFactory exceptionFactory;

    private boolean failed = false;

    public LoadWorker(
            VirtuosoConfiguration configuration,
            ExceptionFactory exceptionFactory) {
        this.configuration = configuration;
        this.exceptionFactory = exceptionFactory;
    }

    @Override
    public Object call() {
        LOG.info("Loading ...");
        Connection loaderConnection = null;
        try {
            loaderConnection = getSqlConnection();
            try (Statement statementRun = loaderConnection.createStatement()) {
                final ResultSet resultSetRun =
                        statementRun.executeQuery("rdf_loader_run()");
                resultSetRun.close();
            }
        } catch (SQLException | LpException ex) {
            failed = false;
            LOG.info("Loading ... failed", ex);
        } finally {
            try {
                if (loaderConnection != null) {
                    loaderConnection.close();
                }
            } catch (SQLException ex) {
                LOG.warn("Can't close SQL connection.", ex);
            }
        }
        LOG.info("Loading ... done");
        return null;
    }

    public boolean isFailed() {
        return failed;
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
