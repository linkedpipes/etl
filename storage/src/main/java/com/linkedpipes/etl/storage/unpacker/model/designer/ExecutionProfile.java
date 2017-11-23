package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionProfile implements Loadable {

    private String rdfRepositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;

    private String rdfRepositoryType = LP_PIPELINE.NATIVE_STORE;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                rdfRepositoryPolicy = value.asString();
                return null;
            case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                rdfRepositoryType = value.asString();
                return null;
            case LP_PIPELINE.HAS_LOG_POLICY:
                logPolicy = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getRdfRepositoryPolicy() {
        return rdfRepositoryPolicy;
    }

    public String getRdfRepositoryType() {
        return rdfRepositoryType;
    }

    public String getLogPolicy() {
        return logPolicy;
    }

}
