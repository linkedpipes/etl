package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineControlFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.library.pipeline.model.PipelineVertex;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Change pipeline and all related resources.
 */
public class ChangePipelineResource {

    @FunctionalInterface
    public interface PluginTemplateSource {

        PluginTemplate apply(Resource resource) throws StorageException;

    }

    private final Function<Resource, Resource> templateMapping;

    private final PluginTemplateSource referenceTemplateSource;

    public ChangePipelineResource(
            Function<Resource, Resource> templateMapping,
            PluginTemplateSource referenceTemplateSource) {
        this.templateMapping = templateMapping;
        this.referenceTemplateSource = referenceTemplateSource;
    }

    public Pipeline localize(Pipeline pipeline, Resource resource)
            throws StorageException {

        PipelineExecutionProfile remoteProfile = pipeline.executionProfile();
        PipelineExecutionProfile profile = new PipelineExecutionProfile(
                PipelineExecutionProfile.createResource(resource),
                remoteProfile.rdfRepositoryPolicy(),
                remoteProfile.rdfRepositoryType(),
                remoteProfile.logRetentionPolicy(),
                remoteProfile.debugDataRetentionPolicy(),
                remoteProfile.failedExecutionLimit(),
                remoteProfile.successfulExecutionLimit());

        List<PipelineComponent> components =
                new ArrayList<>(pipeline.components().size());
        for (PipelineComponent component : pipeline.components()) {
            components.add(updateComponent(component, resource));
        }

        List<PipelineDataFlow> dataFlows = pipeline.dataFlows()
                .stream().map(item -> updateDataFlow(item, resource))
                .toList();

        List<PipelineControlFlow> controlFlows = pipeline.controlFlows()
                .stream().map(item -> updateControlFlow(item, resource))
                .toList();

        return new Pipeline(
                resource,
                pipeline.created(), LocalDateTime.now(),
                pipeline.label(), pipeline.version(), pipeline.note(),
                pipeline.tags(),
                profile, components, dataFlows, controlFlows);
    }

    private PipelineComponent updateComponent(
            PipelineComponent remote, Resource pipeline)
            throws StorageException {
        Resource local = updateResource(remote.resource(), pipeline);
        Resource template = templateMapping.apply(remote.template());
        if (template == null) {
            // Template might have not been known or may be a plugin.
            template = remote.template();
        }
        PluginTemplate plugin = referenceTemplateSource.apply(template);
        if (plugin == null) {
            throw new StorageException("Missing template: '{}'", template);
        }
        return new PipelineComponent(
                local,
                remote.label(), remote.description(), remote.note(),
                remote.color(), remote.x(), remote.y(),
                template,
                remote.disabled(),
                ConfigurationFacade.localizeConfiguration(
                        plugin.configurationDescription(),
                        remote.configuration().selector(),
                        local),
                ConfigurationFacade.configurationGraph(local));
    }

    private Resource updateResource(Resource resource, Resource pipeline) {
        if (resource instanceof IRI) {
            String suffix = getUrlSuffix(resource);
            return SimpleValueFactory.getInstance().createIRI(
                    pipeline + suffix);
        } else {
            return resource;
        }
    }

    private PipelineDataFlow updateDataFlow(
            PipelineDataFlow remote, Resource pipeline) {
        Resource local = updateResource(remote.resource(), pipeline);
        return new PipelineDataFlow(
                local,
                updateResource(remote.source(), pipeline),
                remote.sourceBinding(),
                updateResource(remote.target(), pipeline),
                remote.target(),
                updateVertices(remote.vertices(), pipeline));
    }

    private List<PipelineVertex> updateVertices(
            List<PipelineVertex> remote, Resource pipeline) {
        List<PipelineVertex> result = new ArrayList<>(remote.size());
        for (PipelineVertex item : remote) {
            result.add(new PipelineVertex(
                    updateResource(item.resource(), pipeline),
                    item.order(), item.x(), item.y()));
        }
        return result;
    }

    private PipelineControlFlow updateControlFlow(
            PipelineControlFlow remote, Resource pipeline) {
        Resource local = updateResource(remote.resource(), pipeline);
        return new PipelineControlFlow(
                local,
                updateResource(remote.source(), pipeline),
                updateResource(remote.target(), pipeline),
                updateVertices(remote.vertices(), pipeline));
    }

    private String getUrlSuffix(Resource resource) {
        String content = resource.stringValue();
        return content.substring(content.lastIndexOf("/") + 1);
    }

}
