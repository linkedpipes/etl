package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class SqlExecutor {

    private static final String SQL_LD_DIR = "ld_dir (?, ?, ?)";

    private static final String SQL_QUERY_WAITING =
            "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 0";

    private static final String SQL_DELETE_LOAD_LIST =
            "delete from DB.DBA.load_list where ll_file like ?";

    private static final String SQL_QUERY_FINISHED =
            "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 2";

    public static final String SQL_LOAD = "rdf_loader_run()";

    public static final String SQL_CHECKPOINT = "checkpoint";

    private final String url;

    private final String username;

    private final String password;

    private final String directory;

    private final ExceptionFactory exceptionFactory;

    public SqlExecutor(String url, String username, String password,
                       String directory, ExceptionFactory exceptionFactory) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.directory = directory;
        this.exceptionFactory = exceptionFactory;
    }

    public void insertLoadRecord(String fileName, String graph)
            throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement = createLdStatement(
                    connection, directory, fileName, graph)) {
                statement.executeQuery().close();
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't execute ld_dir query.", ex);
        }
    }

    private static PreparedStatement createLdStatement(Connection connection,
                                                       String directory, String fileName, String graph)
            throws SQLException {
        final PreparedStatement statement =
                connection.prepareStatement(SQL_LD_DIR);
        statement.setString(1, directory);
        statement.setString(2, fileName);
        statement.setString(3, graph);
        return statement;
    }

    private Connection getSqlConnection() throws LpException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't create sql connection.", ex);
        }
    }

    public int getFilesToLoad() throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement =
                         createQueryWaitingStatement(connection, directory)) {
                return executeStatementForSingleInt(statement);
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't query load_list table.", ex);
        }
    }

    private static PreparedStatement createQueryWaitingStatement(
            Connection connection, String directory) throws SQLException {
        final PreparedStatement statement =
                connection.prepareStatement(SQL_QUERY_WAITING);
        statement.setString(1, directory + "%");
        return statement;
    }

    private int executeStatementForSingleInt(PreparedStatement statement)
            throws SQLException {
        try (ResultSet resultSetProcessing = statement.executeQuery()) {
            resultSetProcessing.next();
            return resultSetProcessing.getInt(1);
        }
    }

    public void clearLoadList() throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement = prepareClearStatement(
                    connection, directory)) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure("Can't clear loading table.", ex);
        }
    }

    private static PreparedStatement prepareClearStatement(
            Connection connection, String directory) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                SQL_DELETE_LOAD_LIST);
        statement.setString(1, directory + "%");
        return statement;
    }

    public int getFilesLoaded(String directory) throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement = prepareLoadedFilesStatement(
                    connection, directory)) {
                return executeStatementForSingleInt(statement);
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure(
                    "Can't get number of loaded files.", ex);
        }
    }

    private static PreparedStatement prepareLoadedFilesStatement(
            Connection connection, String directory) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(
                SQL_QUERY_FINISHED);
        statement.setString(1, directory + "%");
        return statement;
    }

    /**
     * Blocking function.
     */
    public void loadData() throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement =
                         prepareStatement(connection, SQL_LOAD)) {
                statement.executeQuery();
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure(
                    "Can't load data.", ex);
        }
    }

    public PreparedStatement prepareStatement(
            Connection connection, String statement)
            throws SQLException {
        return connection.prepareStatement(statement);
    }

    public void checkpoint() throws LpException {
        try (Connection connection = getSqlConnection()) {
            try (PreparedStatement statement =
                         prepareStatement(connection, SQL_CHECKPOINT)) {
                statement.executeQuery();
            }
        } catch (SQLException ex) {
            throw exceptionFactory.failure(
                    "Can't load data.", ex);
        }
    }

}
