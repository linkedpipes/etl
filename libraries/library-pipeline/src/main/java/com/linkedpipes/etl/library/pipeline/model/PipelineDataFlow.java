package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * Connect ports on a single component.
 */
public class PipelineDataFlow extends PipelineConnection {

    private final Value sourceBinding;

    private final Value targetBinding;

    public PipelineDataFlow(
            Resource resource,
            Resource source,
            Resource target,
            Value sourceBinding,
            Value targetBinding
    ) {
        super(resource, source, target);
        this.sourceBinding = sourceBinding;
        this.targetBinding = targetBinding;
    }

    public Value sourceBinding() {
        return sourceBinding;
    }

    public Value targetBinding() {
        return targetBinding;
    }

}
