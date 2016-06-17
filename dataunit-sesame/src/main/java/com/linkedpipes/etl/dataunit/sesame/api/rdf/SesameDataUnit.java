package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException.Message;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.executor.api.v1.exception.RecoverableException;
import java.util.Arrays;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Škoda Petr
 */
public interface SesameDataUnit {

    public class SesameDataUnitException extends NonRecoverableException {

        public SesameDataUnitException(String messages, Object... args) {
            super(Arrays.asList(new Message(messages, "en")), args);
        }

    }

    public class RepositoryActionFailed extends SesameDataUnitException {

        public RepositoryActionFailed(String messages, Object... args) {
            super(messages, args);
        }

    }

    @FunctionalInterface
    public interface RepositoryFunction<T> {

        public T accept(RepositoryConnection connection) throws
                RecoverableException, NonRecoverableException, OpenRDFException;

    }

    @FunctionalInterface
    public interface RepositoryProcedure {

        public void accept(RepositoryConnection connection) throws
                RecoverableException, NonRecoverableException, OpenRDFException;

    }

    @FunctionalInterface
    public interface Procedure {

        public void accept() throws
                RecoverableException, NonRecoverableException, OpenRDFException;

    }

    public void execute(RepositoryProcedure action) throws
            RepositoryActionFailed;

    public <T> T execute(RepositoryFunction<T> action) throws
            RepositoryActionFailed;

    public void execute(Procedure action) throws RepositoryActionFailed;

    public Repository getRepository();

}
