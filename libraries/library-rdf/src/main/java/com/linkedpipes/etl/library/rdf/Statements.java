package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for statements, there are extensions of this class
 * that provides additional functionality.
 */
public class Statements implements Collection<Statement> {

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    protected final Collection<Statement> collection;

    protected Statements(Collection<Statement> collection) {
        this.collection = collection;
    }

    public static Statements wrap(Collection<Statement> collection) {
        if (collection == null) {
            return arrayList();
        }
        return new Statements(collection);
    }

    public static Statements set() {
        return new Statements(new HashSet<>());
    }

    public static Statements empty() {
        return new Statements(Collections.emptyList());
    }

    public static Statements arrayList() {
        return new Statements(new ArrayList<>());
    }

    public static Statements arrayList(int size) {
        return new Statements(new ArrayList<>(size));
    }

    public static Statements arrayList(Map<?, Statements> map) {
        Statements result = new Statements(new ArrayList<>());
        map.values().forEach(result::addAll);
        return result;
    }

    public static Statements readOnly(Collection<Statement> collection) {
        if (collection == null) {
            return null;
        }
        return new Statements(Collections.unmodifiableCollection(collection));
    }

    public StatementsBuilder builder() {
        return new StatementsBuilder(collection);
    }

    public StatementsFile file() {
        return new StatementsFile(collection);
    }

    public StatementsSelector selector() {
        return new StatementsSelector(collection);
    }

    public Statements withGraph(String graph) {
        return withGraph(valueFactory.createIRI(graph));
    }

    public Statements withGraph(Resource graph) {
        List<Statement> result = new ArrayList<>(collection.size());
        collection.stream()
                .map(statement -> valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject(),
                        graph
                ))
                .forEach(result::add);
        return wrap(result);
    }

    public Statements withoutGraph() {
        List<Statement> result = new ArrayList<>(collection.size());
        collection.stream()
                .map(statement -> valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject()
                ))
                .forEach(result::add);
        return wrap(result);
    }

    @Override
    public boolean add(Statement statement) {
        return collection.add(statement);
    }

    public boolean addAll(Collection<Statement> statements, Resource graph) {
        boolean result = false;
        for (Statement statement : statements) {
            Statement integratedStatement = valueFactory.createStatement(
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject(),
                    graph);
            result |= collection.add(integratedStatement);
        }
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends Statement> collection) {
        if (collection == null) {
            return false;
        }
        return this.collection.addAll(collection);
    }

    public Map<Resource, Statements> splitByGraph() {
        Map<Resource, Statements> result = new HashMap<>();
        for (Statement statement : collection) {
            Resource graph = statement.getContext();
            result.computeIfAbsent(graph, (key) -> Statements.arrayList())
                    .add(statement);
        }
        return result;
    }

    public List<Statement> asList() {
        if (collection instanceof List) {
            return (List<Statement>) collection;
        }
        return new ArrayList<>(collection);
    }

    public Collection<Resource> subjects() {
        Set<Resource> result = new HashSet<>();
        for (Statement statement : collection) {
            result.add(statement.getSubject());
        }
        return result;
    }

    public Collection<Value> objects() {
        Set<Value> result = new HashSet<>();
        for (Statement statement : collection) {
            result.add(statement.getObject());
        }
        return result;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return collection.contains(object);
    }

    @Override
    public Iterator<Statement> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return collection.toArray(array);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.collection.containsAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.collection.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.collection.retainAll(collection);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public int hashCode() {
        return this.collection.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Statements other) {
            return collection.equals(other.collection);
        }
        return false;
    }

}
