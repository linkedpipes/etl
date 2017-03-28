package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface Rdf4jDataUnit {

    @FunctionalInterface
    interface RepositoryFunction<T> {

        T accept(RepositoryConnection connection) throws LpException;

    }

    @FunctionalInterface
    interface RepositoryProcedure {

        void accept(RepositoryConnection connection) throws LpException;

    }

    @FunctionalInterface
    interface Procedure {

        void accept() throws LpException;

    }

    void execute(RepositoryProcedure action) throws LpException;

    <T> T execute(RepositoryFunction<T> action) throws LpException;

    void execute(Procedure action) throws LpException;

    Repository getRepository();

}
