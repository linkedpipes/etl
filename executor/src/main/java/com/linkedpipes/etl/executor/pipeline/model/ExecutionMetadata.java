package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionMetadata implements Loadable {

    private boolean deleteWorkingData = false;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    @Override
    public Loadable load(String predicate, BackendRdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_DELETE_WORKING_DATA:
                deleteWorkingData = object.asBoolean();
                return null;
            case LP_PIPELINE.HAS_LOG_POLICY:
                logPolicy = object.asString();
                return null;
            default:
                return null;
        }
    }

    public boolean isDeleteWorkingData() {
        return deleteWorkingData;
    }

    public String getLogPolicy() {
        return logPolicy;
    }

}
