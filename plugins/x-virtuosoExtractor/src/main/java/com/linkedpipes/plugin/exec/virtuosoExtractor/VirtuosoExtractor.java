package com.linkedpipes.plugin.exec.virtuosoExtractor;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 *
 * @author Škoda Petr
 */
public final class VirtuosoExtractor implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(VirtuosoExtractor.class);

    private static final String SQL_DUMP
            = "dump_one_graph ('%s', '%s', 1000000000)";

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
     * @throws SQLException
     */
    private void executeSqlStatement(String command) throws SQLException {
        LOG.info("Executing statement: {}", command);
        try (Connection connection = DriverManager.getConnection(
                configuration.getVirtuosoUrl(),
                configuration.getUsername(),
                configuration.getPassword())) {
            // Execute statement.
            try (Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery(command) ) {
                    // We don't need the result.
                }
            }
        }
    }

}
