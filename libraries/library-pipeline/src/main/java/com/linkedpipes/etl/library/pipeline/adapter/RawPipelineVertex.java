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

}
