package com.linkedpipes.executor.rdf.controller;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Should make handling common connection logic easier.
 *
 * @author Škoda Petr
 */
public final class ConnectionAction {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAction.class);

    public static class CallFailed extends Exception {

        public CallFailed(Throwable cause) {
            super(cause);
        }

    }

    @FunctionalInterface
    public static interface Action {

        public void accept(RepositoryConnection connection) throws Exception;

    }

    @FunctionalInterface
    public static interface Producer<T> {

        public T accept(RepositoryConnection connection) throws Exception;

    }

    private ConnectionAction() {
    }

    /**
     * Execute call that does not return anything, it's assumed that call interact by side-effects with the rest of the
     * environment.
     *
     * @param repository
     * @param supplier
     * @throws com.linkedpipes.executor.rdf.controller.ConnectionAction.CallFailed
     */
    public static void call(Repository repository, Action supplier) throws CallFailed {
        RepositoryConnection connection = null;
        try {
            connection = repository.getConnection();
            supplier.accept(connection);
        } catch (Exception ex) {
            throw new CallFailed(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.info("Can't close repository.", ex);
                }
            }
        }
    }

    /**
     * Execute call that returns some value.
     *
     * @param <T>
     * @param repository
     * @param function
     * @return
     * @throws com.linkedpipes.executor.rdf.controller.ConnectionAction.CallFailed
     */
    public static <T> T call(Repository repository, Producer<T> function) throws CallFailed {
        RepositoryConnection connection = null;
        try {
            connection = repository.getConnection();
            return function.accept(connection);
        } catch (Exception ex) {
            throw new CallFailed(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.info("Can't close repository.", ex);
                }
            }
        }

    }

}
