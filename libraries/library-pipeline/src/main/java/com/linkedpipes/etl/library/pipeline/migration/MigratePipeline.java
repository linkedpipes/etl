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

    public Pipeline migrate(RawPipeline template)
            throws PipelineMigrationFailed {
        int initialVersion = template.version;
        if (initialVersion < 1) {
            (new PipelineV0(templateToPlugin.keySet(), true))
                    .migrateToV1(template);
        }
        if (initialVersion < 2) {
            (new PipelineV1(templateToPlugin)).migrateToV2(template);
        }
        return template.toPipeline();
    }

}
