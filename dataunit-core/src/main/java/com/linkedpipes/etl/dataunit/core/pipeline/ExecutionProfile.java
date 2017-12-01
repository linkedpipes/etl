package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ExecutionProfile implements Loadable {

    private String rdfRepositoryPolicy;

    private String rdfRepositoryType;

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                rdfRepositoryPolicy = value.asString();
                break;
            case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                rdfRepositoryType = value.asString();
                break;
        }
        return null;
    }

    public String getRdfRepositoryPolicy() {
        return rdfRepositoryPolicy;
    }

    public String getRdfRepositoryType() {
        return rdfRepositoryType;
    }

    public static ExecutionProfile getDefault() {
        ExecutionProfile profile = new ExecutionProfile();
        profile.rdfRepositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;
        profile.rdfRepositoryType = LP_PIPELINE.NATIVE_STORE;
        return profile;
    }

}
