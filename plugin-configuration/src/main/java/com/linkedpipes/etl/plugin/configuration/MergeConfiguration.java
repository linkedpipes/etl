package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.model.vocabulary.LP;
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

public class MergeConfiguration {

    /**
     * Store all statements connected to the described entity via
     * given predicate.
     */
    private static class PredicateTree {

        public String control = LP.NONE;

        public List<Statement> statements = new ArrayList<>();

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
            Description description,
            String baseIri, IRI graph) throws InvalidConfiguration {
        List<Statement> result;
        if (description.getGlobalControl() == null) {
            result = mergeMemberControlled(
                    parentRdf, instanceRdf, description, baseIri);
        } else {
            result = mergeGloballyControlled(
                    parentRdf, instanceRdf, description, baseIri);
        }
        return RdfUtils.setGraph(result, graph);
    }

    private List<Statement> mergeGloballyControlled(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description, String baseIri
    ) throws InvalidConfiguration {
        Resource parentResource = findEntity(parentRdf, description);
        Resource instanceResource = findEntity(instanceRdf, description);
        String parentControl =
                getGlobalControl(description, parentRdf, parentResource);
        String instanceControl =
                getGlobalControl(description, instanceRdf, instanceResource);
        //
        return mergeGlobal(
                parentRdf, instanceRdf, description,
                parentResource, instanceResource,
                parentControl, instanceControl, baseIri);
    }

    private Resource findEntity(
            List<Statement> statements, Description description) {
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (statement.getObject().equals(description.getType())) {
                return statement.getSubject();
            }
        }
        return null;
    }

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

    private String getGlobalControl(
            Description description, List<Statement> statements,
            Resource resource) {
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            if (statement.getPredicate().equals(
                    description.getGlobalControl())) {
                return statement.getObject().stringValue();
            }
        }

        return LP.NONE;
    }

    private List<Statement> mergeGlobal(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description,
            Resource parentResource,
            Resource instanceResource,
            String parentControl,
            String instanceControl,
            String baseIri) throws InvalidConfiguration {
        switch (parentControl) {
            case LP.FORCE:
            case LP.FORCED:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.NONE:
                break;
            case LP.INHERIT_AND_FORCE:
            case LP.INHERIT:
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        parentControl, parentResource);
        }
        switch (instanceControl) {
            case LP.FORCE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.NONE:
                return replaceControlAndSubject(
                        instanceRdf,
                        instanceResource,
                        description.getGlobalControl(),
                        LP.NONE,
                        baseIri);
            case LP.INHERIT_AND_FORCE:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.FORCED,
                        baseIri);
            case LP.INHERIT:
                return replaceControlAndSubject(
                        parentRdf,
                        parentResource,
                        description.getGlobalControl(),
                        LP.NONE,
                        baseIri);
            case LP.FORCED:
            default:
                throw new InvalidConfiguration(
                        "Invalid control: {} for: {}",
                        parentControl, parentResource);
        }
    }

    private List<Statement> replaceControlAndSubject(
            List<Statement> statements, Resource resource, IRI predicate,
            String control, String baseIri) {
        List<Statement> result = new ArrayList<>(statements);
        result.removeIf(statement ->
                statement.getSubject().equals(resource)
                        && statement.getPredicate().equals(predicate));
        result.add(valueFactory.createStatement(
                resource, predicate, valueFactory.createIRI(control)));
        RdfUtils.updateSubject(
                result, resource,
                valueFactory.createIRI(baseIri + "/1"));
        return result;
    }

    private List<Statement> mergeMemberControlled(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description, String baseIri
    ) throws InvalidConfiguration {
        Resource parentResource = findEntity(parentRdf, description);
        Resource instanceResource = findEntity(instanceRdf, description);
        Map<Description.Member, PredicateTree> parent =
                loadTreesForPredicates(description, parentRdf, parentResource);
        Map<Description.Member, PredicateTree> instance =
                loadTreesForPredicates(
                        description, instanceRdf, instanceResource);
        List<Statement> result = selectEssentials(parentRdf, parentResource);
        for (Description.Member member : description.getMembers()) {
            PredicateTree parentTree = parent.get(member);
            PredicateTree instanceTree = instance.get(member);
            result.addAll(mergeTrees(
                    parentResource, parentTree,
                    instanceResource, instanceTree));
        }
        RdfUtils.updateSubject(
                result, parentResource,
                valueFactory.createIRI(baseIri + "/1"));
        return result;
    }

    private Map<Description.Member, PredicateTree> loadTreesForPredicates(
            Description description, List<Statement> statements,
            Resource resource) {
        Map<Description.Member, PredicateTree> result = new HashMap<>();
        Map<IRI, PredicateTree> dataPredicates = new HashMap<>();
        Map<IRI, PredicateTree> controlPredicates = new HashMap<>();
        for (Description.Member member : description.getMembers()) {
            PredicateTree predicateTree = new PredicateTree(member);
            result.put(member, predicateTree);
            dataPredicates.put(member.getProperty(), predicateTree);
            controlPredicates.put(member.getControl(), predicateTree);
        }
        //
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            loadDataPredicate(statements, statement, dataPredicates);
            loadControlPredicate(statement, controlPredicates);
        }
        return result;
    }

    private void loadDataPredicate(
            List<Statement> statements, Statement statement,
            Map<IRI, PredicateTree> collector) {
        IRI predicate = statement.getPredicate();
        Value value = statement.getObject();
        if (collector.containsKey(predicate)) {
            PredicateTree predicateTree = collector.get(predicate);
            predicateTree.statements.add(statement);
            // Collect sub-tree if there is any.
            if (value instanceof Resource) {
                predicateTree.statements.addAll(
                        selectSubTree(statements, (Resource) value));
            }
        }
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
            PredicateTree tree, String controlPredicate,
            Resource from, Resource to) {
        List<Statement> result = new ArrayList<>();
        result.add(valueFactory.createStatement(
                to, tree.member.getControl(),
                valueFactory.createIRI(controlPredicate)));
        for (Statement statement : tree.statements) {
            if (statement.getSubject().equals(from)) {
                result.add(valueFactory.createStatement(
                        to, statement.getPredicate(),
                        statement.getObject()));
            } else {
                result.add(statement);
            }
        }
        return result;
    }

}
