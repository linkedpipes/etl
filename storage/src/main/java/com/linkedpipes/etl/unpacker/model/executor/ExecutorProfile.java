package com.linkedpipes.etl.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;

public class ExecutorProfile {

    private final String iri;

    private String repositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;

    private String repositoryType = LP_PIPELINE.NATIVE_STORE;

    public ExecutorProfile(String iri) {
        this.iri = iri;
    }

    public void write(StatementsBuilder builder) {
        // TODO Add class for ExecutionProfile
        builder.addIri(
                iri, LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY, repositoryPolicy);
        builder.addIri(
                iri, LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE, repositoryType);
    }

    public String getIri() {
        return iri;
    }

    public void setRepositoryPolicy(String repositoryPolicy) {
        this.repositoryPolicy = repositoryPolicy;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

}
