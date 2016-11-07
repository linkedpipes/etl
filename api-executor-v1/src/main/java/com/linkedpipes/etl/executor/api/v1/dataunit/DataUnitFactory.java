package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

public interface DataUnitFactory {

    /**
     * Create {@link ManageableDataUnit}. Returned data unit must not be
     * initialized.
     *
     * @param definition
     * @param resourceIri
     * @param graph
     * @return Null if this factory can not create given type of dataunit.
     */
    public ManageableDataUnit create(SparqlSelect definition,
            String resourceIri, String graph) throws RdfException;

}
