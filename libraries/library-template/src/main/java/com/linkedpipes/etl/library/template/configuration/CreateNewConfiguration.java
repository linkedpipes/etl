package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class CreateNewConfiguration {

    private static final IRI CONTROL_NONE;

    private static final IRI CONTROL_INHERIT;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        CONTROL_NONE = valueFactory.createIRI(LP_V1.NONE);
        CONTROL_INHERIT = valueFactory.createIRI(LP_V1.INHERIT);
    }

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public List<Statement> createNewFromJarFile(
            List<Statement> configuration,
            ConfigurationDescription description,
            String baseIri, Resource graph) {
        return createNew(configuration, description,
                baseIri, graph, CONTROL_NONE);
    }

    public List<Statement> createNewFromTemplate(
            List<Statement> configuration,
            ConfigurationDescription description,
            String baseIri, Resource graph) {
        return createNew(configuration, description,
                baseIri, graph, CONTROL_INHERIT);
    }

    private List<Statement> createNew(
            List<Statement> configuration,
            ConfigurationDescription description,
            String baseIri, Resource graph,
            IRI defaultControl) {
        int counter = 1;
        Map<Resource, List<Statement>> entities = splitBySubject(configuration);
        for (var entry : entities.entrySet()) {
            if (!isOfType(entry.getValue(), description.configurationType())) {
                continue;
            }
            replaceControls(
                    entry.getValue(), description,
                    entry.getKey(), defaultControl);
            removeSubstitution(
                    entry.getValue(), description);
            StatementsUtils.renameSubject(
                    entry.getValue(),
                    entry.getKey(),
                    valueFactory.createIRI(baseIri + "/" + counter));
        }
        return collectStatements(entities).withGraph(graph).asList();
    }

    private Map<Resource, List<Statement>> splitBySubject(
            List<Statement> statements) {
        Map<Resource, List<Statement>> result = new HashMap<>();
        for (Statement statement : statements) {
            Resource resource = statement.getSubject();
            result.computeIfAbsent(resource, (subject) -> new ArrayList<>())
                    .add(statement);
        }
        return result;
    }

    private boolean isOfType(List<Statement> statements, Resource type) {
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (!statement.getObject().equals(type)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void replaceControls(
            List<Statement> statements,
            ConfigurationDescription description,
            Resource resource, IRI defaultControl) {
        // Prepare array with default values.
        Map<IRI, Value> controls =
                createDefaultControls(description, defaultControl);
        // Replace.
        statements.removeIf(
                (statement -> controls.containsKey(statement.getPredicate())));
        statements.addAll(createControlStatements(resource, controls));
    }

    private Map<IRI, Value> createDefaultControls(
            ConfigurationDescription description, IRI defaultControl) {
        Map<IRI, Value> result = new HashMap<>();
        for (var member : description.members().values()) {
            result.put(member.control(), defaultControl);
        }
        return result;
    }

    private List<Statement> createControlStatements(
            Resource resource, Map<IRI, Value> controls) {
        List<Statement> result = new ArrayList<>();
        for (var entry : controls.entrySet()) {
            result.add(valueFactory.createStatement(
                    resource,
                    entry.getKey(),
                    entry.getValue()
            ));
        }
        return result;
    }

    /**
     * Remove all substitution related content as the new configuration
     * must not contain any of it.
     */
    private void removeSubstitution(
            List<Statement> statements, ConfigurationDescription description) {
        Set<IRI> forRemoval = description.members().values().stream()
                .map(ConfigurationDescription.Member::substitution)
                .collect(Collectors.toSet());
        statements.removeIf(
                (statement -> forRemoval.contains(statement.getPredicate())));
    }

    private Statements collectStatements(
            Map<Resource, List<Statement>> entities) {
        Statements result = Statements.arrayList();
        for (List<Statement> statements : entities.values()) {
            result.addAll(statements);
        }
        return result;
    }

}
