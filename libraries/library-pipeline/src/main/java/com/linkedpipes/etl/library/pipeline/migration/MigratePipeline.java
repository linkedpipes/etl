package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;

public class MigratePipeline {

    /**
     * For a given reference template provides URL of a plugin template.
     */
    private final Map<Resource, Resource> templateToPlugin;

    public MigratePipeline(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    /**
     * Does not change given pipeline.
     */
    public Pipeline migrate(RawPipeline pipeline) throws PipelineMigrationFailed {
        RawPipeline working = new RawPipeline(pipeline);
        int initialVersion = pipeline.version;
        if (initialVersion < 1) {
            (new PipelineV0(templateToPlugin.keySet(), true))
                    .migrateToV1(working);
        }
        if (initialVersion < 2) {
            (new PipelineV1(templateToPlugin)).migrateToV2(working);
        }
        if (initialVersion < 5) {
            (new PipelineV2()).migrateToV5(working);
        }
        return working.toPipeline();
    }

    public static boolean shouldMigrate(RawPipeline pipeline) {
        return pipeline.version < 5;
    }

}
