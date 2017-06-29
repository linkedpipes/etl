package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.InvalidNumberOfResults;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;

public class PipelineInfo {

    private final String pipeline;

    private final String graph;

    private final RdfSource source;

    public PipelineInfo(String pipeline, String graph, RdfSource source) {
        this.pipeline = pipeline;
        this.graph = graph;
        this.source = source;
    }

    public String getRdfRepositoryPolicy() throws RdfUtilsException {
        try {
            return RdfUtils.sparqlSelectSingle(source,
                    getRdfRepositoryPolicyQuery(), "rdfPolicy");
        } catch (InvalidNumberOfResults ex) {
            return LP_PIPELINE.SINGLE_REPOSITORY;
        }
    }

    private String getRdfRepositoryPolicyQuery() {
        return "SELECT ?rdfPolicy FROM <" + graph + "> WHERE {" +
                " <" + pipeline + "> <" + LP_PIPELINE.HAS_PROFILE + "> " +
                "?profile . " +
                " ?profile <" + LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY + "> " +
                "?rdfPolicy ." +
                "}";
    }

}
