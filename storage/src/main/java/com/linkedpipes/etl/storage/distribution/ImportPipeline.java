package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.pipeline.PipelineLoader;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.distribution.model.ImportPipelineOptions;
import com.linkedpipes.etl.storage.pipeline.ChangePipelineResource;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.PluginTemplateFacade;
import com.linkedpipes.etl.storage.template.ReferenceTemplateFacade;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We assume all templates has already been imported from the pipeline.
 */
public class ImportPipeline {

    public static class Container {

        private final RawPipeline rawPipeline;

        private Exception exception;

        /**
         * Pipeline in local form ready to be stored.
         */
        private final Pipeline loadedPipeline;

        private Pipeline localPipeline;

        private boolean stored = false;

        public Container(PipelineLoader.Container source) {
            this.rawPipeline = source.rawPipeline();
            this.exception = source.exception();
            this.loadedPipeline = source.pipeline();
        }

        public RawPipeline raw() {
            return rawPipeline;
        }

        public Exception exception() {
            return exception;
        }

        public Pipeline remote() {
            return loadedPipeline;
        }

        public Pipeline local() {
            return localPipeline;
        }

        public boolean stored() {
            return stored;
        }

    }

    private final PluginTemplateFacade pluginFacade;

    private final ReferenceTemplateFacade referenceFacade;

    private final PipelineFacade pipelineFacade;

    private final Map<Resource, Resource> templateRemoteToLocal;

    private final List<Container> containers = new ArrayList<>();

    public ImportPipeline(
            PluginTemplateFacade pluginFacade,
            ReferenceTemplateFacade referenceFacade,
            PipelineFacade pipelineFacade,
            Map<Resource, Resource> templateRemoteToLocal) {
        this.pluginFacade = pluginFacade;
        this.referenceFacade = referenceFacade;
        this.pipelineFacade = pipelineFacade;
        this.templateRemoteToLocal = templateRemoteToLocal;
    }

    public void loadFromStatements(StatementsSelector statements)
            throws StorageException {
        PipelineLoader loader = new PipelineLoader(prepareTemplateToPlugin());
        loader.loadAndMigrate(statements);
        loader.getContainers().stream()
                .map(Container::new)
                .forEach(containers::add);
    }

    /**
     * Given mapping is from remote templates to local templates.
     * This change it to remote templates to plugins.
     */
    private Map<Resource, Resource> prepareTemplateToPlugin()
            throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        for (var entry : templateRemoteToLocal.entrySet()) {
            if (pluginFacade.isPluginTemplate(entry.getValue())) {
                // It is a plugin.
                result.put(entry.getKey(), entry.getValue());
                continue;
            }
            ReferenceTemplate local = referenceFacade.getReferenceTemplate(
                    entry.getValue());
            if (local == null) {
                // Use available mapping.
                result.put(entry.getKey(), entry.getValue());
            } else {
                // Replace with plugin.
                result.put(entry.getKey(), local.plugin());
            }
        }
        return result;
    }

    /**
     * Perform import operation with a single pipeline, fail if
     * multiple pipelines are given.
     */
    public Pipeline importPipeline(ImportPipelineOptions options)
            throws StorageException {
        if (containers.size() != 1) {
            throw new StorageException(
                    "Invalid number of pipelines '{}'.",
                    containers.size());
        }
        importPipelines(List.of(options));
        Container container = containers.get(0);
        if (container.localPipeline == null || container.exception != null) {
            throw new StorageException("Can't import pipeline.");
        }
        return container.localPipeline;
    }

    /**
     * Return successfully imported pipelines.
     */
    public void importPipelines(List<ImportPipelineOptions> importOptions) {
        Map<Resource, ImportPipelineOptions> optionsMap =
                buildOptionsMap(importOptions);
        for (Container container : containers) {
            if (container.loadedPipeline == null) {
                continue;
            }
            ImportPipelineOptions option = optionsMap.get(
                    container.loadedPipeline.resource());
            try {
                importPipeline(container, option);
            } catch (StorageException ex) {
                container.exception = ex;
            }
        }
    }

    private Map<Resource, ImportPipelineOptions> buildOptionsMap(
            List<ImportPipelineOptions> options) {
        Map<Resource, ImportPipelineOptions> result = new HashMap<>();
        if (options.size() == 1 && options.get(0).pipeline == null) {
            // If only one configuration is given, replicate it
            // for each given pipeline.
            ImportPipelineOptions template = options.get(0);
            // One configuration for all pipelines.
            for (Container container : containers) {
                if (container.loadedPipeline == null) {
                    continue;
                }
                Resource resource = container.loadedPipeline.resource();
                result.put(resource, template.copyForPipeline(resource));
            }
        } else {
            // Just map the options to pipelines.
            for (ImportPipelineOptions item : options) {
                result.put(item.pipeline, item);
            }
        }
        return result;
    }

    private void importPipeline(
            Container container, ImportPipelineOptions option)
            throws StorageException {
        Resource resource = prepareLocalResource(
                container.loadedPipeline, option);
        container.localPipeline = updatePipeline(
                container.loadedPipeline, resource, option.targetLabel);
        if (option.importPipeline) {
            pipelineFacade.storePipeline(container.localPipeline);
            container.stored = true;
        }
    }

    private Resource prepareLocalResource(
            Pipeline pipeline, ImportPipelineOptions options) {
        if (options.targetResource != null) {
            return options.targetResource;
        } else if (options.keepPipelineUrl) {
            return pipeline.resource();
        } else if (options.keepPipelineSuffix) {
            String suffix = getUrlSuffix(pipeline.resource());
            return pipelineFacade.createPipelineResourceWithSuffix(suffix);
        } else {
            return pipelineFacade.reservePipelineResource();
        }
    }

    private Pipeline updatePipeline(
            Pipeline remote, Resource resource, String label)
            throws StorageException {
        if (label == null) {
            return updatePipeline(remote, resource);
        }
        return updatePipeline(new Pipeline(
                remote.resource(),
                remote.created(), remote.lastUpdate(),
                label, remote.version(), remote.note(),
                remote.tags(), remote.executionProfile(),
                remote.components(), remote.dataFlows(),
                remote.controlFlows()), resource);
    }

    private Pipeline updatePipeline(
            Pipeline pipeline, Resource resource) throws StorageException {
        ChangePipelineResource worker = new ChangePipelineResource(
                templateRemoteToLocal::get,
                referenceFacade::findPluginTemplate
        );
        return worker.localize(pipeline, resource);
    }

    private String getUrlSuffix(Resource resource) {
        String content = resource.stringValue();
        return content.substring(content.lastIndexOf("/") + 1);
    }

    public List<Container> getContainers() {
        return Collections.unmodifiableList(containers);
    }

}
