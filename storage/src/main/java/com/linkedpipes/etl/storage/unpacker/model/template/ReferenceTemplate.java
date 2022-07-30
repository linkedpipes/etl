package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Value;

public class ReferenceTemplate extends Template {

    public static final String TYPE = LP_PIPELINE.REFERENCE_TEMPLATE;

    private String template;

    private String configGraph;

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                configGraph = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_TEMPLATE:
                template = value.stringValue();
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
