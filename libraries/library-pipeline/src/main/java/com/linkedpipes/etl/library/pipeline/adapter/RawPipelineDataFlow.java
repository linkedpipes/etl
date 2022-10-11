package com.linkedpipes.etl.library.pipeline.adapter;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

public class RawPipelineDataFlow {

    /**
     * Connection resource.
     */
    public Resource resource;

    /**
     * Connection source component.
     */
    public Resource source;

    /**
     * Source binding.
     */
    public Value sourceBinding;

    /**
     * Connection target component.
     */
    public Resource target;

    /**
     * Target binding.
     */
    public Value targetBinding;

    /**
     * Vertices.
     */
    public final List<RawPipelineVertex> vertices = new ArrayList<>();

}
