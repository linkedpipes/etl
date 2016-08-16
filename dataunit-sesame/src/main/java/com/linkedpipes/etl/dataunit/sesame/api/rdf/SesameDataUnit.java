package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * @author Škoda Petr
 */
public interface SesameDataUnit {

    @FunctionalInterface
    public interface RepositoryFunction<T> {

        public T accept(RepositoryConnection connection) throws LpException;

    }

    @FunctionalInterface
    public interface RepositoryProcedure {

        public void accept(RepositoryConnection connection) throws LpException;

    }

    @FunctionalInterface
    public interface Procedure {

        public void accept() throws LpException;

    }

    public void execute(RepositoryProcedure action) throws LpException;

    public <T> T execute(RepositoryFunction<T> action) throws LpException;

    public void execute(Procedure action) throws LpException;

    public Repository getRepository();

}
