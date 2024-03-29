package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.Collections;
import java.util.List;

/**
 * Connect ports on a single component.
 */
public record PipelineDataFlow(
        /*
         * Connection resource.
         */
        Resource resource,
        /*
         * Connection source component.
         */
        Resource source,
        /*
         * Source binding.
         */
        Value sourceBinding,
        /*
         * Connection target template.
         */
        Resource target,
        /*
         * Target binding.
         */
        Value targetBinding,
        /*
         * Vertices.
         */
        List<PipelineVertex> vertices
) {

    public PipelineDataFlow {
        vertices = Collections.unmodifiableList(vertices);
    }

}
