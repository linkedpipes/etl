package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.rdf.entity.EntityReference;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.*;
import java.util.stream.Collectors;

class SubstituteEnvironment {

    /**
     * Represents a value for substitution.
     * We can have more than one value or substitution value.
     */
    static class Substitution {

        public List<Value> values = new ArrayList<>();

        public List<Value> substitutions = new ArrayList<>();

    }

    public static EntityReference substitute(
            Map<String, String> env,
            Rdf4jSource referenceSource, EntityReference reference,
            String configurationType)
            throws RdfUtilsException, ExecutorException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();

        List<Statement> statements = collectStatements(
                referenceSource, reference.getGraph());

        // Create map of predicates and substitute predicates for given resource.
        // We use convention that the substitution predicate
        // is the predicate + "Substitution";
        // The idea is to share the same internal, so we can
        // access the same data using predicate as well as
        // substitution predicate.
        Map<IRI, Map<Resource, Substitution>> predicateMap = new HashMap<>();
        Map<IRI, Map<Resource, Substitution>> substitutionMap = new HashMap<>();
        for (var iri : collectPredicates(referenceSource, configurationType)) {
            Map<Resource, Substitution> value = new HashMap<>();
            predicateMap.put(iri, value);
            var substitutionIri = valueFactory.createIRI(iri + "Substitution");
            substitutionMap.put(substitutionIri, value);
        }

        // Next we iterate the statements searching for values or
        // predicates and substitute predicates.
        // We need to pair them together for substitution.
        // Other statements we just pass along.
        List<Statement> nextStatements = new ArrayList<>();
        for (Statement statement : statements) {
            Resource subject = statement.getSubject();
            IRI predicate = statement.getPredicate();

            if (predicateMap.containsKey(predicate)) {
                Objects.requireNonNull(predicateMap.get(predicate))
                                .computeIfAbsent(subject, key -> new Substitution())
                                .values.add(statement.getObject());
            } else if (substitutionMap.containsKey(predicate)) {
                Objects.requireNonNull(substitutionMap.get(predicate))
                        .computeIfAbsent(subject, key -> new Substitution())
                        .values.add(statement.getObject());
            } else {
                nextStatements.add(statement);
            }
        }

        // Next we need to assemble back statements in
        // predicateMap or substitute them by the substitutions.
        for (var predicateEntry : predicateMap.entrySet()) {
            IRI predicate = predicateEntry.getKey();
            for (var resourceEntry : predicateEntry.getValue().entrySet()) {
                Resource subject = resourceEntry.getKey();
                var value = resourceEntry.getValue();
                if (value.substitutions.isEmpty()) {
                    // We keep the original values.
                    for (var object : value.values) {
                        nextStatements.add(valueFactory.createStatement(
                                subject, predicate, object));
                    }
                } else {
                    // We use the values from substitution.
                    for (var object : value.substitutions) {
                        // We support substitution only for strings.
                        var nextObject =  valueFactory.createLiteral(
                                substitute(env, object.stringValue()));
                        nextStatements.add(valueFactory.createStatement(
                                subject, predicate, nextObject));
                    }
                }

            }
        }

        // At the last step we just store the statements in a new store
        // using a new graph.
        IRI nextGraph = valueFactory.createIRI(
                reference.getGraph() + "/substituted");
        Rdf4jSource nextSource = Rdf4jSource.createInMemory();
        addStatements(nextStatements, nextGraph, nextSource);

        return new EntityReference(
                reference.getResource(), nextGraph.stringValue(), nextSource);
    }

    /**
     * @return All controlled predicates for given configuration type.
     */
    private static Set<IRI> collectPredicates(
            Rdf4jSource referenceSource, String configurationType)
            throws RdfUtilsException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DefaultControl control = new DefaultControl();
        control.loadDefinition(referenceSource, configurationType);
        return control.getControlledPredicates().stream()
                .map(DefaultControl.PropertyControl::predicate)
                .map(valueFactory::createIRI)
                .collect(Collectors.toSet());
    }

    /**
     * @return All statements in given graph.
     */
    private static List<Statement> collectStatements
            (Rdf4jSource source, String graph) {
        List<Statement> result = new ArrayList<>();
        IRI graphIri = SimpleValueFactory.getInstance().createIRI(graph);
        try (var connection = source.getRepository().getConnection()) {
            connection.getStatements(null, null, null, graphIri)
                    .iterator()
                    .forEachRemaining(result::add);
        }
        return result;
    }

    private static void addStatements(
            List<Statement> statements, IRI graph, Rdf4jSource target) {
        try (var connection = target.getRepository().getConnection()) {
            connection.add(statements, graph);
        }
    }

    static String substitute(Map<String, String> env, String value)
            throws ExecutorException {
        StringBuilder result = new StringBuilder();
        StringBuilder token = new StringBuilder();
        boolean readingToken = false;
        for (int index = 0; index < value.length(); ++index) {
            char character = value.charAt(index);
            if (readingToken) {
                if (character == '}') {
                    readingToken = false;
                    result.append(resolveEnvironment(env, token.toString()));
                    token.setLength(0);
                } else {
                    token.append(character);
                }
            } else {
                if (character == '{') {
                    readingToken = true;
                } else {
                    result.append(character);
                }
            }
        }
        return result.toString();
    }

    private static String resolveEnvironment(
            Map<String, String> env, String name) throws ExecutorException {
        if (!name.startsWith("LP_ETL_")) {
            throw new ExecutorException(
                    "Environment property '{}' for substitution must starts" +
                            " with LP_ETL_.", name);
        }
        String result = env.get(name);
        if (result == null) {
            throw new ExecutorException(
                    "Missing environment property '{}' for substitution",
                    name);
        }
        return result;
    }

}
