package com.linkedpipes.etl.library.template.plugin.adapter;

import com.linkedpipes.etl.library.template.configuration.adapter.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.plugin.model.PluginType;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Information about dialogs is not loaded using this class.
 * The reason is that we do not store enough information into RDF in
 * first place. In addition, this method is employed to load data from user
 * provided RDF specification of a component that does not
 * contain that information.
 */
public class RdfToPluginTemplate {

    /**
     * Manually assign given configuration to all loaded templates.
     */
    public static List<PluginTemplate> asPluginTemplates(
            StatementsSelector statements) {
        Collection<Resource> resources =
                statements.selectByType(LP_V1.JAR_TEMPLATE)
                        .subjects();
        List<PluginTemplate> result = new ArrayList<>();
        for (Resource resource : resources) {
            if (!resource.isIRI()) {
                continue;
            }
            PluginTemplate template = loadPluginTemplate(
                    statements, (IRI) resource);
            if (template == null) {
                continue;
            }
            result.add(template);
        }
        return result;
    }

    private static PluginTemplate loadPluginTemplate(
            StatementsSelector statements, IRI pluginResource) {
        String label = null, color = null;
        PluginType type = null;
        String documentation = null;
        boolean supportControl = false;
        List<String> tags = new ArrayList<>();
        List<PluginTemplate.Port> ports = new ArrayList<>();
        IRI configurationGraph = null, configurationDescriptionGraph = null;

        for (Statement statement : statements.withSubject(pluginResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.PREF_LABEL:
                    label = value.stringValue();
                    break;
                case LP_V1.HAS_COLOR:
                    color = value.stringValue();
                    break;
                case LP_V1.HAS_COMPONENT_TYPE:
                    type = PluginType.fromIri(value.stringValue());
                    break;
                case LP_V1.HAS_SUPPORT_CONTROL:
                    if (value.isLiteral()) {
                        supportControl = ((Literal) value).booleanValue();
                    }
                    break;
                case LP_V1.HAS_KEYWORD:
                    tags.add(value.stringValue());
                    break;
                case LP_V1.HAS_INFO_LINK:
                    documentation = value.stringValue();
                    break;
                case LP_V1.HAS_PORT:
                    if (value.isResource()) {
                        ports.add(loadPort(statements, (Resource) value));
                    }
                    break;
                case LP_V1.HAS_DIALOG:
                    // Dialogs are not loaded see class comment.
                    break;
                case LP_V1.HAS_CONFIGURATION_GRAPH:
                    if (value.isIRI()) {
                        configurationGraph = (IRI) value;
                    }
                    break;
                case LP_V1.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                    if (value.isIRI()) {
                        configurationDescriptionGraph = (IRI) value;
                    }
                    break;
                default:
                    break;
            }
        }

        if (configurationGraph == null) {
            configurationGraph =
                    PluginTemplate.defaultConfigurationGraph(
                            pluginResource);
        }
        Statements configuration =
                statements.selectByGraph(configurationGraph).withoutGraph();

        if (configurationDescriptionGraph == null) {
            configurationDescriptionGraph =
                    PluginTemplate.defaultConfigurationDescriptionGraph(
                            pluginResource);
        }

        List<ConfigurationDescription> descriptions =
                RdfToConfigurationDescription.asConfigurationDescriptions(
                        statements.selector());

        if (descriptions.size() != 1) {
            return null;
        }

        return new PluginTemplate(
                pluginResource, label, color, type, supportControl,
                tags, documentation, null, ports, configuration,
                configurationGraph, descriptions.get(0),
                configurationDescriptionGraph);
    }

    private static PluginTemplate.Port loadPort(
            StatementsSelector statements, Resource resource) {
        String binding = null, label = null;
        List<String> types = new ArrayList<>();
        for (Statement statement : statements.withSubject(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_BINDING:
                    binding = value.stringValue();
                    break;
                case LP_V1.PREF_LABEL:
                    label = value.stringValue();
                    break;
                case LP_V1.TYPE:
                    types.add(value.stringValue());
                    break;
                default:
                    break;
            }
        }
        return new PluginTemplate.Port(binding, label, types);
    }

}
