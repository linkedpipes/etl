package com.linkedpipes.etl.library.pipeline;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.RdfToRawPipeline;
import com.linkedpipes.etl.library.pipeline.migration.MigratePipeline;
import com.linkedpipes.etl.library.pipeline.migration.PipelineMigrationFailed;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PipelineLoader {

    public static class Container {

        private final RawPipeline rawPipeline;

        private Exception exception;

        private Pipeline pipeline;

        public Container(RawPipeline rawPipeline) {
            this.rawPipeline = rawPipeline;
        }

        public RawPipeline rawPipeline() {
            return rawPipeline;
        }

        public Exception exception() {
            return exception;
        }

        public Pipeline pipeline() {
            return pipeline;
        }

        public boolean isFailed() {
            return exception != null;
        }

    }

    /**
     * For a given reference template provides URL of a plugin template.
     */
    private final Map<Resource, Resource> templateToPlugin;

    private final List<Container> containers = new ArrayList<>();

    public PipelineLoader(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    public void loadAndMigrate(Statements statements) {
        List<RawPipeline> rawPipelines =
                RdfToRawPipeline.asRawPipelines(statements);
        for (RawPipeline rawPipeline : rawPipelines) {
            Container container = new Container(rawPipeline);
            containers.add(container);
            Pipeline pipeline;
            try {
                pipeline = loadPipeline(rawPipeline);
            } catch (PipelineMigrationFailed ex) {
                container.exception = ex;
                continue;
            }
            container.pipeline = pipeline;
        }
    }

    public Pipeline loadPipeline(RawPipeline rawPipeline)
            throws PipelineMigrationFailed {
        MigratePipeline migration = new MigratePipeline(templateToPlugin);
        return migration.migrate(rawPipeline);
    }

    public List<Container> getContainers() {
        return Collections.unmodifiableList(containers);
    }

    public List<Pipeline> getPipelines() {
        return containers.stream()
                .filter(container -> !container.isFailed())
                .map(Container::pipeline)
                .toList();
    }

    public List<Pipeline> getMigratedPipelines() {
        return containers.stream()
                .filter(container -> !container.isFailed())
                .map(Container::pipeline)
                .toList();
    }

    public boolean hasAnyFailed() {
        for (Container container : containers) {
            if (container.isFailed()) {
                return true;
            }
        }
        return false;
    }

}
