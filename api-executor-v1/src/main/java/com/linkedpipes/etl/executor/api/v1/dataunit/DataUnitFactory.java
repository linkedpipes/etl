package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 *
 * @author Škoda Petr
 */
public interface DataUnitFactory {

    public class CreationFailed extends Exception {

        public CreationFailed(String message) {
            super(message);
        }

        public CreationFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Create {@link ManagableDataUnit} that implements given interfaces. Returned object should not yet been
     * initialized.
     *
     * @param definition
     * @param resourceUri
     * @param graph
     * @return Null if this factory can not create {@link ManagableDataUnit} with given interfaces.
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory.CreationFailed
     */
    public ManagableDataUnit create(SparqlSelect definition, String resourceUri, String graph) throws CreationFailed;

}
