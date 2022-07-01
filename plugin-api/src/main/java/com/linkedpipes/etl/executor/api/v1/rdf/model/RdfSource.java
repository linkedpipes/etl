package com.linkedpipes.etl.executor.api.v1.rdf.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

import java.util.List;

public interface RdfSource {

    @FunctionalInterface
    interface StatementHandler {

        void accept(String predicate, RdfValue value) throws RdfException;

    }

    void statements(String subject, StatementHandler handler)
            throws RdfException;

    List<RdfValue> getPropertyValues(String subject, String predicate)
            throws RdfException;

    List<String> getByType(String type) throws RdfException;

}
