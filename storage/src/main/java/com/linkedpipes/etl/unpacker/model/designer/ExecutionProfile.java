package com.linkedpipes.etl.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Value;

public class ExecutionProfile implements Loadable {

    private String rdfRepositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;

    private String rdfRepositoryType = LP_PIPELINE.NATIVE_STORE;

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY:
                rdfRepositoryPolicy = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE:
                rdfRepositoryType = value.stringValue();
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

}
