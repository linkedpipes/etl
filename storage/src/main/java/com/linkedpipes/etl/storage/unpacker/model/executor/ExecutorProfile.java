package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

public class ExecutorProfile {

    private final String iri;

    private String repositoryType = LP_PIPELINE.SINGLE_REPOSITORY;

    public ExecutorProfile(String iri) {
        this.iri = iri;
    }

    public void write(TripleWriter writer) {
        // TODO Add class for ExecutionProfile
        writer.iri(iri, LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY, repositoryType);
    }

    public String getIri() {
        return iri;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

}
