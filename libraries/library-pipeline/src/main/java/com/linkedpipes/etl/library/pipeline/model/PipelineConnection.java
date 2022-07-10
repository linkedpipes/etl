package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

/**
 * This is not a record as we need to employ inheritance here. Yet we try
 * to remain faithful to the interface.
 */
public abstract class PipelineConnection {

    private final Resource resource;

    private final Resource source;

    private final Resource target;

    protected PipelineConnection(
            Resource resource,
            Resource source,
            Resource target
    ) {
        this.resource = resource;
        this.source = source;
        this.target = target;
    }

    public Resource resource() {
        return resource;
    }

    public Resource source() {
        return source;
    }

    public Resource target() {
        return target;
    }

}
