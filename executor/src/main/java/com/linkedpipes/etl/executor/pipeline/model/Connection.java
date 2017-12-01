package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a connection between components.
 */
public class Connection implements Loadable {

    private final List<String> types = new LinkedList<>();

    private String sourceComponent;

    private String sourceBinding;

    private String targetComponent;

    private String targetBinding;

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object.asString());
                return null;
            case LP_PIPELINE.HAS_SOURCE_BINDING:
                sourceBinding = object.asString();
                return null;
            case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                sourceComponent = object.asString();
                return null;
            case LP_PIPELINE.HAS_TARGET_BINDING:
                targetBinding = object.asString();
                return null;
            case LP_PIPELINE.HAS_TARGET_COMPONENT:
                targetComponent = object.asString();
                return null;
            default:
                return null;
        }
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

    public boolean isDataConnection() {
        return types.contains(LP_PIPELINE.CONNECTION);
    }

}
