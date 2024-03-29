package com.linkedpipes.etl.library.template.plugin.adapter;

import com.linkedpipes.etl.library.template.configuration.adapter.ConfigurationDescriptionToRdf;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class PluginTemplateToRdf {

    public static final String PIPELINE_INPUT =
            "http://etl.linkedpipes.com/resources/components/" +
                    "e-pipelineInput/0.0.0";

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
        result.addIri(resource,
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
            // TODO We require working directory for all, this should be
            //  removed later.
            result.addIri(portResource,
                    LP_V1.REQUIREMENT, LP_V1.WORKING_DIRECTORY);
        }
        // TODO And working directory for the component.
        result.addIri(resource,
                LP_V1.REQUIREMENT, LP_V1.WORKING_DIRECTORY);
        if (resource.stringValue().equals(PIPELINE_INPUT)) {
            result.addIri(resource,
                    LP_V1.REQUIREMENT, LP_V1.INPUT_DIRECTORY);
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
