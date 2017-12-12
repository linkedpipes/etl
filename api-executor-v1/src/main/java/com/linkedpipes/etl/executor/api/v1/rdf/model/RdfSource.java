package com.linkedpipes.etl.executor.api.v1.rdf.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

import java.util.List;

/**
 * TODO Remove information about GRAPH.
 */
public interface RdfSource {

    @FunctionalInterface
    interface StatementHandler {

        void accept(String predicate, RdfValue value) throws RdfException;

    }

    void statements(String graph, String subject, StatementHandler handler)
            throws RdfException;

    List<RdfValue> getPropertyValues(
            String graph, String subject, String predicate)
            throws RdfException;

    List<String> getByType(String graph, String type)
            throws RdfException;

}
