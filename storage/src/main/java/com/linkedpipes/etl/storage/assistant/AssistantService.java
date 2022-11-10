package com.linkedpipes.etl.storage.assistant;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.assistant.adapter.PipelineDesignToRdf;
import com.linkedpipes.etl.storage.assistant.adapter.PipelineToPipelineInfo;
import com.linkedpipes.etl.storage.assistant.model.PipelineDesign;
import com.linkedpipes.etl.storage.assistant.model.PipelineInfo;
import com.linkedpipes.etl.storage.assistant.model.TemplateUseInfo;
import com.linkedpipes.etl.storage.pipeline.PipelineEvents;
import com.linkedpipes.etl.storage.template.TemplateEvents;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Using existing pipeline provide functionality to assist a user.
 * As a side effect this class also provide list of pipelines.
 */
public class AssistantService implements
        PipelineEvents.Listener, TemplateEvents.Listener {

    private Statements cache = Statements.empty();

    private final Map<Resource, PipelineInfo> pipelineInfo = new HashMap<>();

    private final Map<Resource, Resource> templateToParent = new HashMap<>();

    @Override
    public void onPipelineLoaded(Pipeline pipeline) {
        PipelineInfo info = PipelineToPipelineInfo.asPipelineInfo(pipeline);
        pipelineInfo.put(pipeline.resource(), info);
        regenerate();
    }

    private void regenerate() {
        PipelineDesign designInformation = new PipelineDesign();
        pipelineInfo.values().stream()
                .map(PipelineDesign::new)
                .forEach(designInformation::addAll);
        cache = PipelineDesignToRdf.asRdf(designInformation);
    }

    @Override
    public void onPipelineCreated(Pipeline pipeline) {
        PipelineInfo info = PipelineToPipelineInfo.asPipelineInfo(pipeline);
        pipelineInfo.put(pipeline.resource(), info);
        regenerate();
    }

    @Override
    public void onPipelineUpdated(Pipeline previous, Pipeline next) {
        PipelineInfo info = PipelineToPipelineInfo.asPipelineInfo(next);
        pipelineInfo.put(next.resource(), info);
        regenerate();
    }

    @Override
    public void onPipelineDeleted(Pipeline pipeline) {
        pipelineInfo.remove(pipeline.resource());
        regenerate();
    }

    @Override
    public void onPipelineReload() {
        pipelineInfo.clear();
        regenerate();
    }

    @Override
    public void onReferenceTemplateLoaded(ReferenceTemplate template) {
        templateToParent.put(template.resource(), template.template());
    }

    @Override
    public void onReferenceTemplateCreated(ReferenceTemplate template) {
        templateToParent.put(template.resource(), template.template());
    }

    @Override
    public void onReferenceTemplateUpdated(
            ReferenceTemplate previous, ReferenceTemplate next) {
        templateToParent.put(next.resource(), next.template());
    }

    @Override
    public void onReferenceTemplateDeleted(ReferenceTemplate template) {
        templateToParent.remove(template.resource());
    }

    @Override
    public void onReferenceTemplateReload() {
        templateToParent.clear();
    }

    public Statements getDataAsStatements() {
        return cache;
    }

    public List<TemplateUseInfo> getTemplateUseInfo(Resource resource) {
        List<TemplateUseInfo> result = new ArrayList<>();
        Stack<Resource> stack = new Stack<>();
        stack.add(resource);
        boolean first = true;
        while (!stack.isEmpty()) {
            Resource next = stack.pop();
            Resource parent = templateToParent.get(next);
            if (parent == null) {
                // There is no information about this template.
                continue;
            }
            List<PipelineInfo> pipelines = new ArrayList<>();
            for (var pipeline : pipelineInfo.values()) {
                if (!pipeline.templates.containsKey(next)) {
                    continue;
                }
                pipelines.add(pipeline);
            }
            if (first) {
                // We do not export the parent for the root, the first object.
                parent = null;
                first = false;
            }
            result.add(new TemplateUseInfo(next, parent, pipelines));
            // Find all that has next as parent, we check them later.
            for (var entry : templateToParent.entrySet()) {
                if (!Objects.equal(next, entry.getValue())) {
                    continue;
                }
                stack.add(entry.getKey());
            }
        }
        return result;
    }

    public Collection<PipelineInfo> getPipelineInfo() {
        return pipelineInfo.values();
    }

}
