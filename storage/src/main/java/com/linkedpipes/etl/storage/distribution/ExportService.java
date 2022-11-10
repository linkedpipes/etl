package com.linkedpipes.etl.storage.distribution;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * House common export functionality.
 */
class ExportService {

    private final TemplateFacade templateFacade;

    public ExportService(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    public List<ReferenceTemplate> collectTemplates(Pipeline pipeline)
            throws StorageException {
        Map<Resource, ReferenceTemplate> result = new HashMap<>();
        Stack<Resource> stack = new Stack<>();
        for (PipelineComponent component : pipeline.components()) {
            stack.add(component.template());
        }
        while (!stack.isEmpty()) {
            Resource next = stack.pop();
            if (result.containsKey(next) ||
                    templateFacade.isPluginTemplate(next)) {
                continue;
            }
            ReferenceTemplate template =
                    templateFacade.getReferenceTemplate(next);
            if (template == null) {
                // TODO We may need to do something here?
                // This can be missing template or plugin template.
                continue;
            }
            result.put(template.resource(), template);
            stack.add(template.template());
        }
        return result.values().stream().toList();
    }

    public Pipeline removePrivateConfiguration(Pipeline pipeline)
            throws StorageException {
        List<PipelineComponent> components = new ArrayList<>();
        for (PipelineComponent component : pipeline.components()) {
            PluginTemplate pluginTemplate =
                    templateFacade.findPluginTemplate(component.template());
            List<Statement> privateConfiguration =
                    ConfigurationFacade.selectPrivateStatements(
                            component.configuration().selector(),
                            pluginTemplate.configurationDescription());
            Statements configuration = Statements.arrayList();
            configuration.addAll(component.configuration());
            configuration.removeAll(privateConfiguration);
            components.add(new PipelineComponent(
                    component.resource(), component.label(),
                    component.description(), component.note(),
                    component.color(), component.x(), component.y(),
                    component.template(), component.disabled(),
                    configuration, component.configurationGraph()));
        }
        return new Pipeline(
                pipeline.resource(),
                pipeline.created(), pipeline.lastUpdate(), pipeline.label(),
                pipeline.version(), pipeline.note(), pipeline.tags(),
                pipeline.executionProfile(), components, pipeline.dataFlows(),
                pipeline.controlFlows());
    }

    public List<ReferenceTemplate> removePrivateConfiguration(
            List<ReferenceTemplate> templates) throws StorageException {
        List<ReferenceTemplate> result = new ArrayList<>(templates.size());
        for (ReferenceTemplate template : templates) {
            result.add(removePrivateConfiguration(template));
        }
        return result;
    }

    public ReferenceTemplate removePrivateConfiguration(
            ReferenceTemplate template) throws StorageException {
        PluginTemplate pluginTemplate =
                templateFacade.findPluginTemplate(template.plugin());
        if (pluginTemplate == null) {
            // We do not have the template.
            return template;
        }
        List<Statement> privateConfiguration =
                ConfigurationFacade.selectPrivateStatements(
                        template.configuration().selector(),
                        pluginTemplate.configurationDescription());
        Statements configuration = Statements.arrayList();
        configuration.addAll(template.configuration());
        configuration.removeAll(privateConfiguration);
        return new ReferenceTemplate(
                template.resource(), template.version(),
                template.template(), template.plugin(),
                template.label(), template.description(), template.note(),
                template.color(), template.tags(), template.knownAs(),
                configuration, template.configurationGraph()
        );
    }

}
