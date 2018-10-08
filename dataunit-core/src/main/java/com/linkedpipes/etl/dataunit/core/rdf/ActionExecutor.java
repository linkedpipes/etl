package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Provide save way to execute operations with the RDF.
 */
class ActionExecutor {

    private ActionExecutor() {
    }

    public static void execute(
            Repository repository,
            Rdf4jDataUnit.RepositoryProcedure procedure) throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            procedure.accept(connection);
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw new LpException("Can't execute repository procedure.", ex);
        }
    }

    public static <T> T execute(
            Repository repository,
            Rdf4jDataUnit.RepositoryFunction<T> function) throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            return function.accept(connection);
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw new LpException("Can't execute repository function.", ex);
        }
    }

    public static void execute(Rdf4jDataUnit.Procedure procedure)
            throws LpException {
        try {
            procedure.accept();
        } catch (Exception ex) {
            if (ex instanceof LpException) {
                throw (LpException) ex;
            }
            throw new LpException("Can't execute procedure.", ex);
        }
    }

}
