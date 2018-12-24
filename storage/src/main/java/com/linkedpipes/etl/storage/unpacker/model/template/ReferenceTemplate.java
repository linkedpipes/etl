package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class ReferenceTemplate extends Template {

    public static final String TYPE = LP_PIPELINE.REFERENCE_TEMPLATE;

    private String template;

    private String configGraph;

    @Override
    public Loadable load(String predicate, BackendRdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                configGraph = value.asString();
                return null;
            case LP_PIPELINE.HAS_TEMPLATE:
                template = value.asString();
                return null;
            default:
                return super.load(predicate, value);
        }
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public String getConfigGraph() {
        return configGraph;
    }

}
