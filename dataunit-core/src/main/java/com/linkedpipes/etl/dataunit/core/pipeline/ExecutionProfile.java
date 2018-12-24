package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

class ExecutionProfile implements Loadable {

    private String rdfRepositoryPolicy;

    private String rdfRepositoryType;

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                rdfRepositoryPolicy = value.asString();
                break;
            case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                rdfRepositoryType = value.asString();
                break;
            default:
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

}
