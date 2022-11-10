package com.linkedpipes.etl.library.pipeline.adapter;

import org.eclipse.rdf4j.model.Resource;

public class RawPipelineVertex {

    /**
     * Resource of given object.
     */
    public Resource resource;

    /**
     * Define order of the vertex.
     */
    public Integer order;

    /**
     * Position coordinates.
     */
    public Integer x;

    /**
     * Position coordinates.
     */
    public Integer y;

    public RawPipelineVertex() {
    }

    public RawPipelineVertex(RawPipelineVertex other) {
        this.resource = other.resource;
        this.order = other.order;
        this.x = other.x;
        this.y = other.y;
    }

}
