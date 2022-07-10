package com.linkedpipes.etl.library.template.reference.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.rdf.RdfToReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.rdf.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;

import java.util.List;

public class ReferenceTemplateAdapter {

    public static List<ReferenceTemplate> asInstances(Statements statements) {
        return RdfToReferenceTemplate.asReferenceTemplates(
                statements.selector());
    }

    public static List<ReferenceTemplate> asInstances(
            Statements statements, int defaultVersion) {
        return RdfToReferenceTemplate.asReferenceTemplates(
                        statements.selector()).stream()
                .map(item -> new ReferenceTemplate(
                        item.resource(), item.template(), item.label(),
                        item.description(), item.note(), item.color(),
                        item.tags(), item.knownAs(), item.pluginTemplate(),
                        Math.max(item.version(), defaultVersion),
                        item.configuration(), item.configurationGraph()))
                .toList();
    }

    public static Statements definitionAsRdf(ReferenceTemplate definition) {
        return ReferenceTemplateToRdf.definitionAsRdf(definition);
    }

    public static Statements configurationAsRdf(ReferenceTemplate definition) {
        return ReferenceTemplateToRdf.configurationAsRdf(definition);
    }

    public static Statements asRdf(ReferenceTemplate definition) {
        Statements result = Statements.arrayList();
        result.addAll(definitionAsRdf(definition));
        result.addAll(configurationAsRdf(definition));
        return result;
    }

}
