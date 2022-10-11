package com.linkedpipes.etl.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;

public class ExecutorConnection {

    private String iri;

    private String sourceComponent;

    private String sourceBinding;

    private String targetComponent;

    private String targetBinding;

    public ExecutorConnection() {
    }

    public void write(StatementsBuilder builder) {
        builder.addType(iri, LP_PIPELINE.CONNECTION);
        builder.addIri(iri, LP_PIPELINE.HAS_SOURCE_COMPONENT, sourceComponent);
        builder.add(iri, LP_PIPELINE.HAS_SOURCE_BINDING, sourceBinding);
        builder.addIri(iri, LP_PIPELINE.HAS_TARGET_COMPONENT, targetComponent);
        builder.add(iri, LP_PIPELINE.HAS_TARGET_BINDING, targetBinding);
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public String getSourceBinding() {
        return sourceBinding;
    }

    public void setSourceBinding(String sourceBinding) {
        this.sourceBinding = sourceBinding;
    }

    public String getTargetComponent() {
        return targetComponent;
    }

    public void setTargetComponent(String targetComponent) {
        this.targetComponent = targetComponent;
    }

    public String getTargetBinding() {
        return targetBinding;
    }

    public void setTargetBinding(String targetBinding) {
        this.targetBinding = targetBinding;
    }

}
