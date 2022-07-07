package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.rdf.entity.EntityReference;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class SubstituteEnvironment {

    static class StatementPair {

        public Resource resource;

        public IRI predicate;

        public Value value;

        public Value substitute;

        public StatementPair(IRI predicate) {
            this.predicate = predicate;
        }

    }

    public static EntityReference substitute(
            Map<String, String> env,
            Rdf4jSource referenceSource, EntityReference reference,
            String configurationType)
            throws RdfUtilsException, ExecutorException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();

        List<Statement> statements = collectStatements(
                referenceSource, reference.getGraph());

        // Create map of predicates.
        Map<IRI, StatementPair> predicateMap =
                collectPredicates(referenceSource, configurationType)
                        .stream().collect(Collectors.toMap(
                                iri -> iri,
                                StatementPair::new));

        // Create map for substitution.
        Map<IRI, StatementPair> substitutionMap =
                predicateMap.values().stream().collect(Collectors.toMap(
                        item -> valueFactory.createIRI(
                                item.predicate.stringValue() + "Substitution"),
                        item -> item));

        // Collect information.
        List<Statement> nextStatements = new ArrayList<>();
        for (Statement statement : statements) {
            IRI predicate = statement.getPredicate();
            if (predicateMap.containsKey(predicate)) {
                StatementPair pair = predicateMap.get(predicate);
                if (pair.resource != null
                        && pair.resource != statement.getSubject()) {
                    throw new RdfUtilsException("Not supported!");
                }
                pair.resource = statement.getSubject();
                pair.value = statement.getObject();
            } else if (substitutionMap.containsKey(predicate)) {
                StatementPair pair = substitutionMap.get(predicate);
                if (pair.resource != null
                        && pair.resource != statement.getSubject()) {
                    throw new RdfUtilsException("Not supported!");
                }
                pair.resource = statement.getSubject();
                pair.substitute = statement.getObject();
            } else {
                nextStatements.add(statement);
            }
        }

        // Generate back statements.
        for (StatementPair pair : predicateMap.values()) {
            Value value = pair.value;
            if (pair.substitute != null) {
                value = valueFactory.createLiteral(
                        substitute(env, pair.substitute.stringValue()));
            }
            if (pair.resource == null || value == null) {
                continue;
            }
            nextStatements.add(valueFactory.createStatement(
                    pair.resource, pair.predicate, value));
        }

        IRI nextGraph = valueFactory.createIRI(
                reference.getGraph() + "/substituted");
        Rdf4jSource nextSource = Rdf4jSource.createInMemory();
        addStatements(nextStatements, nextGraph, nextSource);

        return new EntityReference(
                reference.getResource(), nextGraph.stringValue(), nextSource);
    }

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
