package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
class ExportPipeline {

    private final TemplateFacade templatesFacade;

    private final MappingFacade mappingFacade;

    private final ConfigurationFacade configurationFacade;

    @Autowired
    public ExportPipeline(
            TemplateFacade templates, MappingFacade mapping) {
        this.templatesFacade = templates;
        this.mappingFacade = mapping;
        this.configurationFacade = new ConfigurationFacade();
    }

    /**
     * Return all used templates in pipelines with their ancestors.
     */
    public Set<Template> getTemplates(
            Pipeline pipeline, Collection<Statement> rdf) {
        Set<Template> templates = new HashSet<>();
        String iri = pipeline.getIri();
        for (Statement statement : rdf) {
            if (!statement.getContext().stringValue().equals(iri)) {
                continue;
            }
            String predicate = statement.getPredicate().stringValue();
            if (!LP_PIPELINE.HAS_TEMPLATE.equals(predicate)) {
                continue;
            }
            String templateIri = statement.getObject().stringValue();
            Template template = templatesFacade.getTemplate(templateIri);
            templates.addAll(templatesFacade.getAncestorsWithoutJarTemplate(
                    template));
        }
        return templates;
    }

    public Collection<Statement> getTemplateRdf(Set<Template> templates)
            throws BaseException {
        List<Statement> output = new LinkedList<>();
        for (Template template : templates) {
            // Use set to remove duplicities.
            Set<Statement> templateRdf = new HashSet<>();
            templateRdf.addAll(templatesFacade.getInterface(template));
            templateRdf.addAll(templatesFacade.getDefinition(template));
            output.addAll(templateRdf);
            output.addAll(templatesFacade.getConfig(template));
        }
        return output;
    }


    public Collection<Statement> getMappingRdf(Set<Template> templates) {
        return mappingFacade.exportForTemplates(templates);
    }

    public void removePrivateConfiguration(Collection<Statement> rdf)
            throws BaseException, InvalidConfiguration {
        // TODO We should check that we are reading from the right graphs.
        Map<Resource, Resource> configurations = new HashMap<>();
        Map<Resource, Resource> types = new HashMap<>();
        Map<Resource, List<Statement>> graphs = new HashMap<>();
        for (Statement statement : rdf) {
            Resource graph = statement.getContext();
            graphs.computeIfAbsent(graph, (value) -> new ArrayList<>())
                    .add(statement);
            String predicate = statement.getPredicate().stringValue();
            if (LP_PIPELINE.HAS_TEMPLATE.equals(predicate)) {
                types.put(statement.getSubject(),
                        (Resource) statement.getObject());
            } else if (LP_PIPELINE.HAS_CONFIGURATION_GRAPH.equals(predicate)) {
                configurations.put(statement.getSubject(),
                        (Resource) statement.getObject());
            }
        }
        //
        for (Map.Entry<Resource, Resource> entry : types.entrySet()) {
            List<Statement> configuration =
                    graphs.get(configurations.get(entry.getKey()));
            Template template = templatesFacade.getTemplate(
                    entry.getValue().stringValue());
            Collection<Statement> description =
                    templatesFacade.getConfigurationDescription(
                            template.getIri());
            Collection<Statement> privateProperties =
                    configurationFacade.selectPrivateStatements(
                            configuration,
                            new ArrayList<>(description));
            rdf.removeAll(privateProperties);
        }
    }

}
