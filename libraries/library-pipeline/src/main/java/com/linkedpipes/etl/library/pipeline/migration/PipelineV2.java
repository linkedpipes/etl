package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;

/**
 * Reason for version 5 was to unify versioning for templates and pipelines.
 */
public class PipelineV2 {

    public void migrateToV5(RawPipeline pipeline) {
        pipeline.version = 5;
    }

}
