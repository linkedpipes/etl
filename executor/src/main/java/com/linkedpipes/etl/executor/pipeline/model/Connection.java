package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a connection between components.
 */
public class Connection implements RdfLoader.Loadable<String> {

    private final List<String> types = new LinkedList<>();

    private String sourceComponent;

    private String sourceBinding;

    private String targetComponent;

    private String targetBinding;

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object);
                return null;
            case LP_PIPELINE.HAS_SOURCE_BINDING:
                sourceBinding = object;
                return null;
            case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                sourceComponent = object;
                return null;
            case LP_PIPELINE.HAS_TARGET_BINDING:
                targetBinding = object;
                return null;
            case LP_PIPELINE.HAS_TARGET_COMPONENT:
                targetComponent = object;
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
