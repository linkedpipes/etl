package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;

import java.time.LocalDateTime;

/**
 * Reason for version 5 was to unify versioning for templates and pipelines.
 */
public class PipelineV2 {

    public void migrateToV5(RawPipeline pipeline) {
        pipeline.version = 5;
        pipeline.lastUpdate = LocalDateTime.now();
    }

}
