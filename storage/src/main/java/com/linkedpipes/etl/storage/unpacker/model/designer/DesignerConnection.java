package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public class DesignerConnection implements Loadable {

    public static final String TYPE = LP_PIPELINE.CONNECTION;

    private String iri;

    private String sourceComponent;

    private String sourceBinding;

    private String targetComponent;

    private String targetBinding;

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                sourceComponent = value.asString();
                return null;
            case LP_PIPELINE.HAS_SOURCE_BINDING:
                sourceBinding = value.asString();
                return null;
            case LP_PIPELINE.HAS_TARGET_COMPONENT:
                targetComponent = value.asString();
                return null;
            case LP_PIPELINE.HAS_TARGET_BINDING:
                targetBinding = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public String getSourceBinding() {
        return sourceBinding;
    }

    public String getTargetComponent() {
        return targetComponent;
    }

    public String getTargetBinding() {
        return targetBinding;
    }

}
