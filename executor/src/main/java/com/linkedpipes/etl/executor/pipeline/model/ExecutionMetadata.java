package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionMetadata implements Loadable {

    private boolean deleteWorkingData = false;

    @Override
    public Loadable load(String predicate, RdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_DELETE_WORKING_DATA:
                deleteWorkingData = object.asBoolean();
                return null;
            default:
                return null;
        }
    }

    public boolean isDeleteWorkingData() {
        return deleteWorkingData;
    }
}
