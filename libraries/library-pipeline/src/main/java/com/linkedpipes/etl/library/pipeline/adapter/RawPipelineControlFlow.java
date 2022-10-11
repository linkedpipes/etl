package com.linkedpipes.etl.library.pipeline.adapter;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

public class RawPipelineControlFlow {

    /**
     * Connection resource.
     */
    public Resource resource;

    /**
     * Connection source component.
     */
    public Resource source;

    /**
     * Connection target component.
     */
    public Resource target;

    /**
     * Vertices.
     */
    public final List<RawPipelineVertex> vertices = new ArrayList<>();

}
