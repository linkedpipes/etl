package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.Procedure;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryFunction;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryProcedure;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Should make handling common connection logic easier.
 *
 * @author Škoda Petr
 */
final class ActionExecutor {

    private ActionExecutor() {
    }

    public static void execute(Repository repository,
            RepositoryProcedure function) throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            function.accept(connection);
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw ExceptionFactory.failure(
                    "Can't execute repository action.", ex);
        }
    }

    public static <T> T execute(Repository repository,
            RepositoryFunction<T> function) throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            return function.accept(connection);
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw ExceptionFactory.failure(
                    "Can't execute repository action.", ex);
        }
    }

    public static void execute(Repository repository, Procedure function)
            throws LpException {
        try {
            function.accept();
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw ExceptionFactory.failure(
                    "Can't execute repository action.", ex);
        }
    }

}
