package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionProfile implements Loadable {

    @Override
    public Loadable load(String predicate, BackendRdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            default:
                return null;
        }
    }

}
