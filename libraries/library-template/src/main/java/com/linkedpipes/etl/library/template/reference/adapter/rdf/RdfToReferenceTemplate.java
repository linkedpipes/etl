package com.linkedpipes.etl.library.template.reference.adapter.rdf;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfToReferenceTemplate {

    public static List<ReferenceTemplate> asReferenceTemplates(
            StatementsSelector statements) {
        Collection<Resource> resources = statements.selectByType(
                LP_V1.REFERENCE_TEMPLATE).subjects();
        List<ReferenceTemplate> result = new ArrayList<>();
        for (Resource resource : resources) {
            result.add(asReferenceTemplate(statements, resource));
        }
        return loadMappingToVersion5(result, statements);
    }

    private static ReferenceTemplate asReferenceTemplate(
            StatementsSelector statements, Resource templateResource) {
        IRI template = null, pluginTemplate = null, knownAs = null;
        Resource configurationGraph = null;
        String prefLabel = null, description = null, note = null;
        int version = 0; // We start with zero as a default version.
        String color = null;
        List<String> tags = new ArrayList<>();

        for (Statement statement : statements.withSubject(templateResource)) {
            Value value = statement.getObject();
            String predicate = statement.getPredicate().stringValue();
            switch (predicate) {
                case LP_V1.HAS_TEMPLATE:
                    if (value instanceof IRI iri) {
                        template = iri;
                    }
                    break;
                case LP_V1.PREF_LABEL:
                    if (value instanceof Literal literal) {
                        prefLabel = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_DESCRIPTION:
                    if (value instanceof Literal literal) {
                        description = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_NOTE:
                    if (value instanceof Literal literal) {
                        note = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_COLOR:
                    if (value instanceof Literal literal) {
                        color = value.stringValue();
                    }
                    break;
                case LP_V1.HAS_KEYWORD:
                    if (value instanceof Literal literal) {
                        tags.add(literal.stringValue());
                    }
                    break;
                // Added in version 3
                case LP_V1.HAS_CONFIGURATION_GRAPH:
                    if (value instanceof Resource resource) {
                        configurationGraph = resource;
                    }
                    break;
                // Added in version 5
                case LP_V1.HAS_PLUGIN_TEMPLATE:
                    if (value instanceof IRI iri) {
                        pluginTemplate = iri;
                    }
                    break;
                case LP_V1.HAS_VERSION:
                    if (value instanceof Literal literal) {
                        version = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_KNOWN_AS:
                    if (value instanceof IRI iri) {
                        knownAs = iri;
                    }
                    break;
            }
        }
        Statements configuration = loadConfiguration(
                statements, templateResource, configurationGraph);
        return new ReferenceTemplate(
                templateResource, template, prefLabel, description, note,
                color, tags, knownAs, pluginTemplate, version,
                configuration, configurationGraph);
    }

    private static Statements loadConfiguration(
            StatementsSelector statements,
            Resource templateResource,
            Resource configurationGraph) {
        if (configurationGraph == null) {
            // We try to guess the configuration graph. The graph was not
            // present in some versions, but there were a convention
            // in naming the configuration graph.
            configurationGraph = SimpleValueFactory.getInstance().createIRI(
                    templateResource.stringValue() + "/configuration");
        }
        return statements.selectByGraph(configurationGraph).withoutGraph();
    }

    /**
     * Mapping information used to be, prior to version 5, in an extra
     * graph.
     */
    private static List<ReferenceTemplate> loadMappingToVersion5(
            List<ReferenceTemplate> templates,
            Collection<Statement> statements) {
        Map<Resource, Resource> localToOriginal = new HashMap<>();
        Resource graph = SimpleValueFactory.getInstance()
                .createIRI(LP_V1.MAPPING_GRAPH);
        statements.stream()
                .filter((s) -> Objects.equal(s.getContext(), graph))
                .filter((s) -> s.getPredicate().equals(OWL.SAMEAS))
                .forEach((s) -> {
                    Resource original = s.getSubject();
                    Value local = s.getObject();
                    if (local instanceof Resource) {
                        localToOriginal.put((Resource) local, original);
                    }
                });
        List<ReferenceTemplate> result = new ArrayList<>();
        for (ReferenceTemplate template : templates) {
            Resource knownAs = localToOriginal.get(template.resource());
            if (template.knownAs() == null && knownAs != null) {
                result.add(new ReferenceTemplate(
                        template.resource(), template.template(),
                        template.label(), template.description(),
                        template.note(), template.color(),
                        template.tags(), knownAs, template.pluginTemplate(),
                        template.version(), template.configuration(),
                        template.configurationGraph()));
            } else {
                result.add(template);
            }
        }
        return result;
    }

}
