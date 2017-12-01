package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

/**
 * Represent instance of component configuration.
 */
public class Configuration implements Loadable {

    private final String iri;

    private Integer order;

    private String graph;

    public Configuration(String iri) {
        this.iri = iri;
    }

    @Override
    public Loadable load(String predicate, RdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_ORDER:
                order = (int)object.asLong();
                return null;
            case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                graph = object.asString();
                return null;
            default:
                return null;
        }
    }

    public Integer getOrder() {
        return order;
    }

    public String getGraph() {
        return graph;
    }

    void check() throws InvalidPipelineException {
        if (order == null) {
            throw new InvalidPipelineException(
                    "Missing configuration order: {}", iri);
        }
    }
}
