package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.Procedure;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryActionFailed;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryFunction;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.RepositoryProcedure;
import org.openrdf.OpenRDFException;
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
            RepositoryProcedure function) throws RepositoryActionFailed {
        try (RepositoryConnection connection = repository.getConnection()) {
            function.accept(connection);
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed(
                    "Operation failed for repository exception.", ex);
        } catch (Exception ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

    public static <T> T execute(Repository repository,
            RepositoryFunction<T> function) throws RepositoryActionFailed {
        try (RepositoryConnection connection = repository.getConnection()) {
            return function.accept(connection);
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed(
                    "Operation failed for repository exception.", ex);
        } catch (Exception ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

    public static void execute(Repository repository, Procedure function)
            throws RepositoryActionFailed {
        try {
            function.accept();
        } catch (OpenRDFException ex) {
            throw new RepositoryActionFailed(
                    "Operation failed for repository exception.", ex);
        } catch (Exception ex) {
            throw new RepositoryActionFailed("Operation failed.", ex);
        }
    }

}
