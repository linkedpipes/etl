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

        // We start by collecting predicates for substitution.
        // We use convention that the substitution predicate is the predicate + "Substitution";
        // For each predicate we remember also the original.
        Map<IRI, IRI> substitutable = new HashMap<>();
        for (var iri : collectPredicates(referenceSource, configurationType)) {
            var substitutionIri = valueFactory.createIRI(iri + "Substitution");
            substitutable.put(substitutionIri, iri);
        }

        // Next we iterate the statements.
        // If there is a predicate from the `substitutable`, we substitute.
        List<Statement> nextStatements = new ArrayList<>();
        // We also store information about the substitution (subject, predicate) as
        // we will drop all non-substituted value later.
        Map<Resource, Set<IRI>> substituted = new HashMap<>();
        for (Statement statement : statements) {
            IRI predicate = statement.getPredicate();
            IRI originalPredicate = substitutable.get(predicate);
            if (originalPredicate == null) {
                // This is not a predicate we can substitute.
                continue;
            }
            // Substitution time.
            var subject = statement.getSubject();
            var nextObject = substitute(env, statement.getObject().stringValue());
            nextStatements.add(valueFactory.createStatement(
                    subject, originalPredicate,
                    valueFactory.createLiteral(nextObject)));
            // We store the original the substitution to be ignored.
            var predicateBlackList = substituted.computeIfAbsent(subject, _ -> new HashSet<>(4));
            predicateBlackList.add(predicate);
            predicateBlackList.add(originalPredicate);
        }

        // Now we add all other statements.
        // We need to do this in two steps as we need to have complete
        // list of `substituted`.
        for (Statement statement : statements) {
            IRI predicate = statement.getPredicate();
            var subject = statement.getSubject();
            var predicateBlackList = substituted.get(subject);
            if (predicateBlackList == null || !predicateBlackList.contains(predicate)) {
                // Ok, it is safe to add this.
                nextStatements.add(statement);
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
