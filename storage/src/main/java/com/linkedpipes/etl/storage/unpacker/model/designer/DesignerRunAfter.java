package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Value;

public class DesignerRunAfter implements Loadable {

    public static final String TYPE = LP_PIPELINE.RUN_AFTER;

    private String iri;

    private String sourceComponent;

    private String targetComponent;

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                sourceComponent = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_TARGET_COMPONENT:
                targetComponent = value.stringValue();
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

    public String getTargetComponent() {
        return targetComponent;
    }

}

