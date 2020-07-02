package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
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

class CreateNewConfiguration {

    private static final IRI CONTROL_NONE;

    private static final IRI CONTROL_INHERIT;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        CONTROL_NONE = valueFactory.createIRI(LP_OBJECTS.NONE);
        CONTROL_INHERIT = valueFactory.createIRI(LP_OBJECTS.INHERIT);
    }

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public List<Statement> createNewFromJarFile(
            List<Statement> configuration, Description description,
            String baseIri, IRI graph) {
        return createNew(configuration, description,
                baseIri, graph, CONTROL_NONE);
    }

    public List<Statement> createNewFromTemplate(
            List<Statement> configuration, Description description,
            String baseIri, IRI graph) {
        return createNew(configuration, description,
                baseIri, graph, CONTROL_INHERIT);
    }

    private List<Statement> createNew(
            List<Statement> configuration, Description description,
            String baseIri, IRI graph, IRI defaultControl) {
        int counter = 1;
        Map<Resource, List<Statement>> entities = splitBySubject(configuration);
        for (var entry : entities.entrySet()) {
            if (!isOfType(entry.getValue(), description.getType())) {
                continue;
            }
            replaceControls(
                    entry.getValue(), description,
                    entry.getKey(), defaultControl);
            RdfUtils.updateSubject(
                    entry.getValue(),
                    entry.getKey(),
                    valueFactory.createIRI(baseIri + "/" + counter));
        }
        List<Statement> result = collectStatements(entities);
        result = RdfUtils.setGraph(result, graph);
        return result;
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
            List<Statement> statements, Description description,
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
            Description description, IRI defaultControl) {
        Map<IRI, Value> result = new HashMap<>();
        for (Description.Member member : description.getMembers()) {
            result.put(member.getControl(), defaultControl);
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

    private List<Statement> collectStatements(
            Map<Resource, List<Statement>> entities) {
        List<Statement> result = new ArrayList<>();
        for (List<Statement> statements : entities.values()) {
            result.addAll(statements);
        }
        return result;
    }

}
