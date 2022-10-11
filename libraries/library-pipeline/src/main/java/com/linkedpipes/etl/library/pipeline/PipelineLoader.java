package com.linkedpipes.etl.library.pipeline;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.rdf.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.migration.MigratePipeline;
import com.linkedpipes.etl.library.pipeline.migration.PipelineMigrationFailed;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PipelineLoader {

    /**
     * For each template store its plugin.
     */
    private final Map<Resource, Resource> templateToPlugin;

    public PipelineLoader(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    public List<Pipeline> loadPipelines(Statements statements)
            throws PipelineMigrationFailed {
        List<RawPipeline> rawPipelines =
                RdfToRawPipeline.asRawPipelines(statements);
        List<Pipeline> result = new ArrayList<>(rawPipelines.size());
        MigratePipeline migration =
                new MigratePipeline(templateToPlugin);
        for (RawPipeline rawPipeline : rawPipelines) {
            result.add(migration.migrate(rawPipeline));
        }
        return result;
    }

}
