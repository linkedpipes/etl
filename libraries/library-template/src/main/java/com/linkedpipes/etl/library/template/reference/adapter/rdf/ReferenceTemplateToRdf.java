package com.linkedpipes.etl.library.template.reference.adapter.rdf;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class ReferenceTemplateToRdf {

    public static Statements definitionAsRdf(
            ReferenceTemplate template) {
        StatementsBuilder result = Statements.arrayList().builder();
        Resource resource = template.resource();
        result.addIri(resource, RDF.TYPE, LP_V1.REFERENCE_TEMPLATE);
        result.add(resource, LP_V1.HAS_TEMPLATE, template.template());
        result.add(resource, LP_V1.PREF_LABEL, template.label());
        result.add(resource, LP_V1.HAS_DESCRIPTION,
                template.description());
        result.add(resource, LP_V1.HAS_NOTE, template.note());
        result.add(resource, LP_V1.HAS_COLOR, template.color());
        for (String keyword : template.tags()) {
            result.add(resource, LP_V1.HAS_KEYWORD, keyword);
        }
        result.add(resource, LP_V1.HAS_KNOWN_AS, template.knownAs());
        result.add(resource, LP_V1.HAS_PLUGIN_TEMPLATE,
                template.plugin());
        result.add(resource, LP_V1.HAS_VERSION, template.version());
        result.add(
                resource, LP_V1.HAS_CONFIGURATION_GRAPH,
                template.configurationGraph());
        return result.withGraph(template.resource());
    }

    public static Statements configurationAsRdf(ReferenceTemplate definition) {
        return definition.configuration()
                .withGraph(definition.configurationGraph());
    }

}
