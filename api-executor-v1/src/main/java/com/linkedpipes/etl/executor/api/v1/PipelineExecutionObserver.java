package com.linkedpipes.etl.executor.api.v1;

import com.linkedpipes.etl.rdf.utils.RdfSource;

/**
 * Observer for pipeline execution events. Can be used by
 * plugins to observe the state of the pipeline.
 */
public interface PipelineExecutionObserver {

    default void onPipelineBegin(String pipeline, String graph,
            RdfSource definition) throws LpException {
        // No operation.
    }

    default void onPipelineEnd() {
        // No operation.
    }

}
