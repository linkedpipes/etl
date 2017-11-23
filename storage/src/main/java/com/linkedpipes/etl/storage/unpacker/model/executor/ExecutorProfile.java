package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

public class ExecutorProfile {

    private final String iri;

    private String repositoryPolicy = LP_PIPELINE.SINGLE_REPOSITORY;

    private String repositoryType = LP_PIPELINE.NATIVE_STORE;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    public ExecutorProfile(String iri) {
        this.iri = iri;
    }

    public void write(TripleWriter writer) {
        // TODO Add class for ExecutionProfile
        writer.iri(iri, LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY, repositoryPolicy);
        writer.iri(iri, LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE, repositoryType);
        writer.iri(iri, LP_PIPELINE.HAS_LOG_POLICY, logPolicy);
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

    public void setLogPolicy(String logPolicy) {
        this.logPolicy = logPolicy;
    }

}
