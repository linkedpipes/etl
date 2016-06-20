package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 *
 * @author Škoda Petr
 */
public interface DataUnitFactory {

    /**
     * Create {@link ManagableDataUnit} that implements given interfaces.
     * Returned object should not yet been initialized.
     *
     * @param definition
     * @param resourceIri
     * @param graph
     * @return {@link ManagableDataUnit} or null.
     * @throws com.linkedpipes.etl.executor.api.v1.RdfException
     */
    public ManagableDataUnit create(SparqlSelect definition, String resourceIri,
            String graph) throws RdfException;

}
