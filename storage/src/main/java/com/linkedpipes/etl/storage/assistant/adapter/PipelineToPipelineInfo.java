package com.linkedpipes.etl.storage.assistant.adapter;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.storage.assistant.model.PipelineInfo;
import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.Map;

public class PipelineToPipelineInfo {

    public static PipelineInfo asPipelineInfo(Pipeline pipeline) {
        PipelineInfo result = new PipelineInfo(
                pipeline.resource(), pipeline.label());
        result.tags.addAll(pipeline.tags());
        Map<Resource, Resource> componentToTemplate =
                buildTemplateMap(pipeline);
        //
        for (PipelineComponent component : pipeline.components()) {
            result.templates.computeIfAbsent(
                    component.template(), PipelineInfo.Template::new);
        }

        // Compute followups.
        for (PipelineDataFlow dataFlow : pipeline.dataFlows()) {
            Resource source = componentToTemplate.get(dataFlow.source());
            Resource target = componentToTemplate.get(dataFlow.target());
            if (source == null || target == null) {
                continue;
            }
            PipelineInfo.Template template = result.templates.get(source);
            template.followup.put(
                    target, template.followup.getOrDefault(target, 0) + 1);
        }
        return result;
    }

    private static Map<Resource, Resource> buildTemplateMap(Pipeline pipeline) {
        Map<Resource, Resource> result = new HashMap<>();
        for (PipelineComponent component : pipeline.components()) {
            result.put(component.resource(), component.template());
        }
        return result;
    }

}
