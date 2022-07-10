package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;

/**
 * Type of connection to force particular execution order.
 */
public class PipelineExecutionFlow extends PipelineConnection {

    public PipelineExecutionFlow(
            Resource resource,
            Resource source,
            Resource target) {
        super(resource, source, target);
    }

}
