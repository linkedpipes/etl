package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

public record PipelineVertex(
        /*
         * Resource of given object.
         */
        Resource resource,
        /*
         * Define order of the vertex.
         */
        Integer order,
        /*
         * Position coordinates.
         */
        Integer x,
        /*
         * Position coordinates.
         */
        Integer y
) {
}
