package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

public interface DataUnitFactory {

    /**
     * Create dataunit.
     *
     * @param dataUnit   Resource of DataUnit to create.
     * @param graph      Graph with the resource.
     * @param definition Pipeline definition.
     * @return Null if this factory can not create given type of dataunit.
     */
    ManageableDataUnit create(
            String dataUnit, String graph, RdfSource definition)
            throws LpException;

}
