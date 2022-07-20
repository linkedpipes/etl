package com.linkedpipes.etl.library.template.plugin.adapter.rdf;

import com.linkedpipes.etl.library.template.configuration.adapter.rdf.ConfigurationDescriptionToRdf;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class PluginTemplateToRdf {

    public static Statements asRdf(PluginTemplate template) {
        StatementsBuilder result = Statements.arrayList().builder();
        result.addAll(definitionAsRdf(template));
        result.addAll(configurationAsRdf(template));
        result.addAll(configurationDescriptionAsRdf(template));
        return result;
    }

    public static Statements definitionAsRdf(PluginTemplate template) {
        StatementsBuilder result = Statements.arrayList().builder();
        Resource resource = template.resource();
        result.setDefaultGraph(resource);
        result.addIri(resource,
                RDF.TYPE, LP_V1.JAR_TEMPLATE);
        result.add(resource,
                LP_V1.PREF_LABEL, template.label());
        result.add(resource,
                LP_V1.HAS_COLOR, template.color());
        result.add(resource,
                LP_V1.HAS_COMPONENT_TYPE, template.type().asIri());
        result.add(
                resource, LP_V1.HAS_SUPPORT_CONTROL,
                template.supportControl());
        for (String keyword : template.tags()) {
            result.add(resource, LP_V1.HAS_KEYWORD, keyword);
        }
        result.add(resource,
                LP_V1.HAS_INFO_LINK, template.documentation());
        result.add(resource,
                LP_V1.HAS_CONFIGURATION_GRAPH,
                template.configurationGraph());
        result.add(
                resource,
                LP_V1.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                template.configurationDescriptionGraph());
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (String dialog : template.dialogs().keySet()) {
            Resource dialogResource = valueFactory.createIRI(
                    template.resource().stringValue()
                            + "/dialog/" + dialog);
            result.add(resource,
                    LP_V1.HAS_DIALOG, dialogResource);
            result.addIri(dialogResource,
                    RDF.TYPE, LP_V1.DIALOG);
            result.add(dialogResource,
                    LP_V1.HAS_NAME, dialog);
        }
        for (PluginTemplate.Port port : template.ports()) {
            Resource portResource = valueFactory.createIRI(
                    template.resource().stringValue()
                            + "/port/" + port.binding());
            result.add(resource, LP_V1.HAS_PORT, portResource);
            for (String type : port.types()) {
                result.addIri(portResource, RDF.TYPE, type);
            }
            result.add(portResource, LP_V1.PREF_LABEL, port.label());
            result.add(portResource, LP_V1.HAS_BINDING, port.binding());
        }
        return result;
    }

    public static Statements configurationAsRdf(PluginTemplate definition) {
        return definition.configuration()
                .withGraph(definition.configurationGraph());
    }

    public static Statements configurationDescriptionAsRdf(
            PluginTemplate definition) {
        return ConfigurationDescriptionToRdf.asRdf(
                definition.configurationDescription(),
                definition.configurationDescriptionGraph());
    }

}
