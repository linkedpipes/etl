package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

class Connection implements Loadable {

    private String sourceComponent;

    private String sourceBinding;

    private String targetComponent;

    private String targetBinding;

    @Override
    public Loadable load(String predicate, RdfValue value) throws RdfException {
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
        }
        return null;
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
