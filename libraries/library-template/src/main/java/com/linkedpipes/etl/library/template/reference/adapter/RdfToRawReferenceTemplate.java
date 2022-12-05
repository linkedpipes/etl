package com.linkedpipes.etl.library.template.reference.adapter;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdfToRawReferenceTemplate {

    public static List<RawReferenceTemplate> asRawReferenceTemplates(
            StatementsSelector statements) {
        var candidates = statements.selectByType(
                LP_V1.REFERENCE_TEMPLATE);
        List<RawReferenceTemplate> result = new ArrayList<>(candidates.size());
        // As we work with statements as list not as a set,
        // single component can be loaded multiple times using resource
        // with given type. To tackle this we filter the candidates here.
        Set<Resource> visited = new HashSet<>();
        for (Statement candidate : candidates) {
            if (visited.contains(candidate.getSubject())) {
                continue;
            }
            visited.add(candidate.getSubject());
            result.add(asRawReferenceTemplate(
                    statements,
                    candidate.getSubject(), candidate.getContext()));
        }
        return loadMappingToVersion5(result, statements);
    }

    private static RawReferenceTemplate asRawReferenceTemplate(
            StatementsSelector statements, Resource templateResource,
            Resource templateGraph) {
        RawReferenceTemplate result = new RawReferenceTemplate();
        result.resource = templateResource;
        //
        for (Statement statement : statements.selector()
                .withSubject(templateResource)) {
            Value value = statement.getObject();
            String predicate = statement.getPredicate().stringValue();
            switch (predicate) {
                case LP_V1.HAS_TEMPLATE:
                    if (value instanceof IRI iri) {
                        result.template = iri;
                    }
                    break;
                case LP_V1.PREF_LABEL:
                    if (value instanceof Literal literal) {
                        result.label = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_DESCRIPTION:
                    if (value instanceof Literal literal) {
                        result.description = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_NOTE:
                    if (value instanceof Literal literal) {
                        result.note = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_COLOR:
                    if (value instanceof Literal literal) {
                        result.color = literal.stringValue();
                    }
                    break;
                case LP_V1.HAS_KEYWORD:
                    if (value instanceof Literal literal) {
                        result.tags.add(literal.stringValue());
                    }
                    break;
                // Added in version 3
                case LP_V1.HAS_CONFIGURATION_GRAPH:
                    if (value instanceof Resource resource) {
                        result.configurationGraph = resource;
                    }
                    break;
                // Added in version 5
                case LP_V1.HAS_PLUGIN_TEMPLATE:
                    if (value instanceof IRI iri) {
                        result.plugin = iri;
                    }
                    break;
                case LP_V1.HAS_VERSION:
                    if (value instanceof Literal literal) {
                        result.version = literal.intValue();
                    }
                    break;
                case LP_V1.HAS_KNOWN_AS:
                    if (value instanceof IRI iri) {
                        result.knownAs = iri;
                    }
                    break;
            }
        }
        loadConfiguration(statements, result);
        return result;
    }

    private static void loadConfiguration(
            StatementsSelector statements, RawReferenceTemplate template) {
        if (template.configurationGraph == null) {
            if (template.resource.isBNode()) {
                // There is no configuration and no way to guess graph.
                template.configuration = Statements.empty();
                return;
            }
            // We try to guess the configuration graph. The graph was not
            // present in some versions, but there were a convention
            // in naming the configuration graph.
            String iri = template.resource.stringValue() + "/configuration";
            template.configurationGraph =
                    SimpleValueFactory.getInstance().createIRI(iri);
        }
        template.configuration =
                statements.selectByGraph(template.configurationGraph)
                        .withoutGraph();
    }

    /**
     * Mapping information used to be, prior to version 5, in an extra
     * graph.
     */
    private static List<RawReferenceTemplate> loadMappingToVersion5(
            List<RawReferenceTemplate> templates,
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
        List<RawReferenceTemplate> result = new ArrayList<>();
        for (RawReferenceTemplate template : templates) {
            Resource knownAs = localToOriginal.get(template.resource);
            if (template.knownAs == null && knownAs != null) {
                template.knownAs = knownAs;
            } else {
                result.add(template);
            }
        }
        return result;
    }

}
