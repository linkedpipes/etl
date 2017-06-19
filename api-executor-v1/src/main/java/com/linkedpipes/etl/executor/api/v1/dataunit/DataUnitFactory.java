package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;

public interface DataUnitFactory {

    /**
     * @param dataUnit Resource of DataUnit to create.
     * @param graph Graph with the resource.
     * @param definition
     * @return Null if this factory can not create given type of dataunit.
     */
    ManageableDataUnit create(String dataUnit, String graph,
            RdfSource definition) throws LpException;

}
