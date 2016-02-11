package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.Procedure;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryActionFailed;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryFunction;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryProcedure;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Should make handling common connection logic easier.
 *
 * TODO We should add possibility for fault tolerance here.
 *
 * @author Škoda Petr
 */
final class ActionExecutor {

    public static void execute(Repository repository, RepositoryProcedure function) throws RepositoryActionFailed {
        try (final RepositoryConnection connection = repository.getConnection()) {
            function.accept(connection);
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed("Operation failed for repository exception.", ex);
        } catch (RecoverableException | NonRecoverableException ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

    public static <T> T execute(Repository repository, RepositoryFunction<T> function) throws RepositoryActionFailed {

        try (final RepositoryConnection connection = repository.getConnection()) {
            return function.accept(connection);
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed("Operation failed for repository exception.", ex);
        } catch (RecoverableException | NonRecoverableException | RuntimeException ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

    public static void execute(Repository repository, Procedure function) throws RepositoryActionFailed {
        try {
            function.accept();
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed("Operation failed for repository exception.", ex);
        } catch (RecoverableException | NonRecoverableException ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

}
