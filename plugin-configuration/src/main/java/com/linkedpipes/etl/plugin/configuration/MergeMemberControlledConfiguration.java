package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.model.vocabulary.LP;
import com.linkedpipes.etl.plugin.configuration.model.Description;
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

        public String control = LP.NONE;

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
        public final Description.Member member;

        public PredicateTree(Description.Member member) {
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
            Description description, String baseIri
    ) throws InvalidConfiguration {
        // Load parent.
        Resource parentResource = RdfUtils.findByType(
                parentRdf, description.getType());
        Map<Description.Member, PredicateTree> parent =
                loadTreesForPredicates(
                        description, parentRdf, parentResource);
        // Load instance.
        Resource instanceResource = RdfUtils.findByType(
                instanceRdf, description.getType());
        Map<Description.Member, PredicateTree> instance =
                loadTreesForPredicates(
                        description, instanceRdf, instanceResource);
        // Merge by member.
        List<Statement> result = selectEssentials(parentRdf, parentResource);
        for (Description.Member member : description.getMembers()) {
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
        RdfUtils.updateSubject(result, parentResource, instanceResource);
        return result;
    }

    private Map<Description.Member, PredicateTree> loadTreesForPredicates(
            Description description, List<Statement> statements,
            Resource resource) {
        // Start be creating trees for all the predicates, i.e. for
        // given property we know the tree.
        Map<Description.Member, PredicateTree> result = new HashMap<>();
        Map<IRI, PredicateTree> dataPredicates = new HashMap<>();
        Map<IRI, PredicateTree> controlPredicates = new HashMap<>();
        Map<IRI, PredicateTree> substitutionPredicates = new HashMap<>();
        for (Description.Member member : description.getMembers()) {
            PredicateTree predicateTree = new PredicateTree(member);
            result.put(member, predicateTree);
            dataPredicates.put(member.getProperty(), predicateTree);
            controlPredicates.put(member.getControl(), predicateTree);
            substitutionPredicates.put(member.getSubstitution(), predicateTree);
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
    ) throws InvalidConfiguration {
        switch (parentTree.control) {
            case LP.FORCE:
            case LP.FORCED:
                return treeToStatement(
                        parentTree,
                        LP.FORCED,
                        parentResource,
                        parentResource);
            case LP.INHERIT:
            case LP.INHERIT_AND_FORCE:
                throw new InvalidConfiguration(
                        "{} control used for template {}",
                        parentTree.control, parentResource);
            case LP.NONE:
                // We decide based on the instance control.
                break;
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        parentTree.control, parentResource);
        }
        // For backward compatibility in mergeHierarchy.trig file.
        if (instanceTree.statements.size() == 0) {
            // If no configuration is provided and control is NONE, we
            // take value from parent.
            if (instanceTree.control.equals(LP.NONE)) {
                instanceTree.control = LP.INHERIT;
            }
        }
        switch (instanceTree.control) {
            case LP.NONE:
                return treeToStatement(
                        instanceTree,
                        LP.NONE,
                        instanceResource,
                        parentResource);
            case LP.FORCE:
                return treeToStatement(
                        instanceTree,
                        LP.FORCED,
                        instanceResource,
                        parentResource);
            case LP.FORCED:
                throw new InvalidConfiguration(
                        "FORCED control used for instance {}",
                        instanceResource);
            case LP.INHERIT_AND_FORCE:
                return treeToStatement(
                        parentTree,
                        LP.FORCED,
                        parentResource,
                        parentResource);
            case LP.INHERIT:
                return treeToStatement(
                        parentTree,
                        LP.NONE,
                        parentResource,
                        parentResource);
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        instanceTree.control, instanceResource);
        }
    }

    private List<Statement> treeToStatement(
            PredicateTree tree, String control,
            Resource from, Resource to) {
        List<Statement> result = new ArrayList<>();
        result.add(valueFactory.createStatement(
                to, tree.member.getControl(),
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
                    to, tree.member.getSubstitution(),
                    tree.substitution));
        }
        return result;
    }

}
