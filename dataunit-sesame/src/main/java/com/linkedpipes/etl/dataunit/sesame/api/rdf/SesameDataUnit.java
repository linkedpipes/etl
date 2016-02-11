package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Škoda Petr
 */
public interface SesameDataUnit {

    public class SesameDataUnitException extends NonRecoverableException {

        public SesameDataUnitException(String message) {
            super(message);
        }

        public SesameDataUnitException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public class RepositoryActionFailed extends SesameDataUnitException {

        public RepositoryActionFailed(String message) {
            super(message);
        }

        public RepositoryActionFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    @FunctionalInterface
    public interface RepositoryFunction<T> {

        public T accept(RepositoryConnection connection) throws RecoverableException, NonRecoverableException, OpenRDFException;

    }

    @FunctionalInterface
    public interface RepositoryProcedure {

        public void accept(RepositoryConnection connection) throws RecoverableException, NonRecoverableException, OpenRDFException;

    }

    @FunctionalInterface
    public interface Procedure {

        public void accept() throws RecoverableException, NonRecoverableException, OpenRDFException;

    }

    public void execute(RepositoryProcedure action) throws RepositoryActionFailed;

    public <T> T execute(RepositoryFunction<T> action) throws RepositoryActionFailed;

    public void execute(Procedure action) throws RepositoryActionFailed;

    public Repository getRepository();

}
