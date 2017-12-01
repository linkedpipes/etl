package com.linkedpipes.plugin.exec.virtuosoExtractor;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public final class VirtuosoExtractor implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(VirtuosoExtractor.class);

    private static final String SQL_DUMP
            = "dump_one_graph ('%s', '%s', 1000000000)";

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public VirtuosoExtractorConfiguration
            configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        //
        try {
            Class.forName("virtuoso.jdbc4.Driver");
        } catch (ClassNotFoundException ex) {
            exceptionFactory.failure("Can't find virtuoso drivers.", ex);
        }
        //
        final String statement = String.format(SQL_DUMP,
                configuration.getGraph(),
                configuration.getOutputPath());
        try {
            executeSqlStatement(statement);
        } catch (SQLException ex) {
            exceptionFactory.failure("Can't execute SQL statement.", ex);
        }
    }

    /**
     * Execute given command.
     *
     * @param command
     */
    private void executeSqlStatement(String command) throws SQLException {
        LOG.info("Executing statement: {}", command);
        try (Connection connection = DriverManager.getConnection(
                configuration.getVirtuosoUrl(),
                configuration.getUsername(),
                configuration.getPassword())) {
            // Execute statement.
            try (Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery(command)) {
                    // We don't need the result.
                }
            }
        }
    }

}
