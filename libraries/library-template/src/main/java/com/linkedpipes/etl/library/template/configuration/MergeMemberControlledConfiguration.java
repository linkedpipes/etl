package com.linkedpipes.etl.library.template.configuration;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MergeMemberControlledConfiguration {

    /**
     * Store all statements connected to the described entity via
     * given predicate.
     */
    private static class PredicateTree {

        public String control = LP_V1.NONE;

        /**
         * All statements for given predicate, this may include whole
         * subtree.
         */
        public List<Statement> statements = new ArrayList<>();

        /**
         * Value with substitution.
         */
        public Value substitution = null;

        /**
         * Link to a description class.
         */
        public final ConfigurationDescription.Member member;

        public PredicateTree(ConfigurationDescription.Member member) {
            this.member = member;
        }

    }

    private static final Set<IRI> ESSENTIAL_PREDICATES = new HashSet<>();

    static {
        ESSENTIAL_PREDICATES.add(RDF.TYPE);
    }

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public List<Statement> merge(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            ConfigurationDescription description, String baseIri
    ) throws ConfigurationException {
        // Load parent.
        Resource parentResource = findEntity(parentRdf, description);
        Map<ConfigurationDescription.Member, PredicateTree> parent =
                loadTreesForPredicates(
                        description, parentRdf, parentResource);
        // Load instance.
        Resource instanceResource = findEntity(instanceRdf, description);
        Map<ConfigurationDescription.Member, PredicateTree> instance =
                loadTreesForPredicates(
                        description, instanceRdf, instanceResource);
        // Merge by member.
        List<Statement> result = selectEssentials(parentRdf, parentResource);
        for (ConfigurationDescription.Member member :
                description.members().values()) {
            PredicateTree parentTree = parent.get(member);
            PredicateTree instanceTree = instance.get(member);
            result.addAll(mergeTrees(
                    parentResource, parentTree,
                    instanceResource, instanceTree));
        }
        // It is possible hat there is no instance configuration.
        if (instanceResource == null) {
            instanceResource = valueFactory.createIRI(baseIri);
        }
        // Rename resource to child, so we are not left
        // with the parent (template) IRI.
        StatementsUtils.renameSubject(result, parentResource, instanceResource);
        return result;
    }

    private Resource findEntity(
            List<Statement> statements,
            ConfigurationDescription description) {
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (statement.getObject().equals(
                    description.configurationType())) {
                return statement.getSubject();
            }
        }
        return null;
    }

    private Map<ConfigurationDescription.Member, PredicateTree> loadTreesForPredicates(
            ConfigurationDescription description, List<Statement> statements,
            Resource resource) {
        // Start be creating trees for all the predicates, i.e. for
        // given property we know the tree.
        Map<ConfigurationDescription.Member, PredicateTree> result = new HashMap<>();
        Map<IRI, PredicateTree> dataPredicates = new HashMap<>();
        Map<IRI, PredicateTree> controlPredicates = new HashMap<>();
        Map<IRI, PredicateTree> substitutionPredicates = new HashMap<>();
        for (ConfigurationDescription.Member member : description.members().values()) {
            PredicateTree predicateTree = new PredicateTree(member);
            result.put(member, predicateTree);
            dataPredicates.put(member.property(), predicateTree);
            controlPredicates.put(member.control(), predicateTree);
            substitutionPredicates.put(member.substitution(), predicateTree);
        }
        // Search through the statements and load.
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            loadDataPredicate(statement, dataPredicates, statements);
            loadControlPredicate(statement, controlPredicates);
            loadSubstitutionPredicate(statement, substitutionPredicates);
        }
        return result;
    }

    private void loadDataPredicate(
            Statement statement,
            Map<IRI, PredicateTree> collector,
            List<Statement> statements) {
        IRI predicate = statement.getPredicate();
        Value value = statement.getObject();
        if (collector.containsKey(predicate)) {
            PredicateTree predicateTree = collector.get(predicate);
            predicateTree.statements.add(statement);
            // Collect subtree if there is any.
            if (value instanceof Resource) {
                predicateTree.statements.addAll(
                        selectSubTree(statements, (Resource) value));
            }
        }
    }

    // TODO This can be moved to utility class.
    private List<Statement> selectSubTree(
            List<Statement> statements, Resource resource) {
        Set<Resource> toVisit = new HashSet<>();
        Set<Resource> visited = new HashSet<>();
        toVisit.add(resource);
        List<Statement> result = new ArrayList<>();
        while (!toVisit.isEmpty()) {
            Set<Resource> nextToVisit = new HashSet<>();
            for (Statement statement : statements) {
                if (!toVisit.contains(statement.getSubject())) {
                    continue;
                }
                result.add(statement);
                if (statement.getObject() instanceof Resource) {
                    nextToVisit.add((Resource) statement.getObject());
                }
            }
            visited.addAll(toVisit);
            toVisit = nextToVisit;
            toVisit.removeAll(visited);
        }
        return result;
    }

    private void loadControlPredicate(
            Statement statement, Map<IRI, PredicateTree> collector) {
        IRI predicate = statement.getPredicate();
        Value value = statement.getObject();
        if (collector.containsKey(predicate)) {
            if (value instanceof IRI) {
                collector.get(predicate).control = value.stringValue();
            }
        }
    }

    private void loadSubstitutionPredicate(
            Statement statement, Map<IRI, PredicateTree> collector) {
        IRI predicate = statement.getPredicate();
        Value value = statement.getObject();
        if (collector.containsKey(predicate)) {
            collector.get(predicate).substitution = value;
        }
    }

    /**
     * Select basic statements that are not controlled by the configuration.
     */
    private List<Statement> selectEssentials(
            List<Statement> statements, Resource resource) {
        return statements.stream()
                .filter(st -> st.getSubject().equals(resource))
                .filter(st -> ESSENTIAL_PREDICATES.contains(st.getPredicate()))
                .collect(Collectors.toList());
    }

    /**
     * Merge trees use parentResource as target subject.
     */
    private List<Statement> mergeTrees(
            Resource parentResource, PredicateTree parentTree,
            Resource instanceResource, PredicateTree instanceTree
    ) throws ConfigurationException {
        switch (parentTree.control) {
            case LP_V1.FORCE:
            case LP_V1.FORCED:
                return treeToStatement(
                        parentTree,
                        LP_V1.FORCED,
                        parentResource,
                        parentResource);
            case LP_V1.INHERIT:
            case LP_V1.INHERIT_AND_FORCE:
                throw new ConfigurationException(
                        "{} control used for template {}",
                        parentTree.control, parentResource);
            case LP_V1.NONE:
                // We decide based on the instance control.
                break;
            default:
                throw new ConfigurationException(
                        "Invalid control: {} for: {}",
                        parentTree.control, parentResource);
        }
        // For backward compatibility in mergeHierarchy.trig file.
        if (instanceTree.statements.size() == 0) {
            // If no configuration is provided and control is NONE, we
            // take value from parent.
            if (instanceTree.control.equals(LP_V1.NONE)) {
                instanceTree.control = LP_V1.INHERIT;
            }
        }
        switch (instanceTree.control) {
            case LP_V1.NONE:
                return treeToStatement(
                        instanceTree,
                        LP_V1.NONE,
                        instanceResource,
                        parentResource);
            case LP_V1.FORCE:
                return treeToStatement(
                        instanceTree,
                        LP_V1.FORCED,
                        instanceResource,
                        parentResource);
            case LP_V1.FORCED:
                throw new ConfigurationException(
                        "FORCED control used for instance {}",
                        instanceResource);
            case LP_V1.INHERIT_AND_FORCE:
                return treeToStatement(
                        parentTree,
                        LP_V1.FORCED,
                        parentResource,
                        parentResource);
            case LP_V1.INHERIT:
                return treeToStatement(
                        parentTree,
                        LP_V1.NONE,
                        parentResource,
                        parentResource);
            default:
                throw new ConfigurationException(
                        "Invalid control: {} for: {}",
                        instanceTree.control, instanceResource);
        }
    }

    private List<Statement> treeToStatement(
            PredicateTree tree, String control,
            Resource from, Resource to) {
        List<Statement> result = new ArrayList<>();
        result.add(valueFactory.createStatement(
                to, tree.member.control(),
                valueFactory.createIRI(control)));
        for (Statement statement : tree.statements) {
            if (statement.getSubject().equals(from)) {
                result.add(valueFactory.createStatement(
                        to, statement.getPredicate(),
                        statement.getObject()));
            } else {
                result.add(statement);
            }
        }
        if (tree.substitution != null) {
            result.add(valueFactory.createStatement(
                    to, tree.member.substitution(),
                    tree.substitution));
        }
        return result;
    }

}
