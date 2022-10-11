package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

import java.util.Collections;
import java.util.List;

/**
 * Type of connection to force particular execution order.
 */
public record PipelineControlFlow(
        /*
         * Connection resource.
         */
        Resource resource,
        /*
         * Connection source component.
         */
        Resource source,
        /*
         * Connection target template.
         */
        Resource target,
        /*
         * Vertices.
         */
        List<PipelineVertex> vertices
) {

    public PipelineControlFlow {
        vertices = Collections.unmodifiableList(vertices);
    }

}
