package com.linkedpipes.etl.library.rdf;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Allow use of selection functions over a collection of statements.
 */
public class StatementsSelector extends Statements {

    private static final String TYPE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private Map<Resource, Statements> resourceIndex = null;

    public StatementsSelector(Collection<Statement> collection) {
        super(collection);
    }

    public List<String> typeAsString(Resource s) {
        return select(s, TYPE, null).objects().stream()
                .filter(Value::isIRI)
                .map(Value::stringValue)
                .toList();
    }

    /**
     * Types for given resource.
     */
    public Collection<Value> types(Resource s) {
        return select(s, TYPE, null).objects();
    }

    public Statements selectIri(Resource s, String p, String o) {
        return select(s, valueFactory.createIRI(p), valueFactory.createIRI(o));
    }

    public Statements selectIri(Resource s, IRI p, String o) {
        return select(s, p, valueFactory.createIRI(o));
    }

    public Statements select(Resource s, String p, Value o) {
        return select(s, valueFactory.createIRI(p), o);
    }

    public Statements select(Resource s, IRI p, Value o) {
        Statements result = Statements.arrayList();
        for (Statement statement : collection) {
            if (s != null && !s.equals(statement.getSubject())) {
                continue;
            }
            if (p != null && !p.equals(statement.getPredicate())) {
                continue;
            }
            if (o != null && !o.equals(statement.getObject())) {
                continue;
            }
            result.add(statement);
        }
        return result;
    }

    public Statements withSubject(Resource s) {
        if (resourceIndex == null) {
            return select(s, (IRI) null, null);
        }
        return resourceIndex.getOrDefault(s, Statements.empty());
    }

    /**
     * Consume memory to speed up {@link #withSubject(Resource)}
     * function call.
     */
    public void buildSubjectIndex() {
        resourceIndex = new HashMap<>();
        for (Statement statement : collection) {
            resourceIndex.computeIfAbsent(
                    statement.getSubject(),
                    resource -> Statements.arrayList()
            ).add(statement);
        }
    }

    public Statements selectByType(String o) {
        return selectByType(valueFactory.createIRI(o));
    }

    public Statements selectByType(IRI o) {
        return select(null, RDF.TYPE, o);
    }

    public Resource selectSubjectOrDefaultIri(
            IRI p, String o, Resource defaultValue) {
        return selectSubjectOrDefault(
                p, valueFactory.createIRI(o), defaultValue);
    }

    public Resource selectSubjectOrDefault(
            IRI p, Value o, Resource defaultValue) {
        Collection<Resource> candidates = select(null, p, o).subjects();
        if (candidates.size() != 1) {
            return defaultValue;
        }
        return candidates.iterator().next();
    }

    public <T extends Throwable> Resource selectSubjectOrThrowIri(
            IRI p, String o, Supplier<T> supplier) throws T {
        return selectSubjectOrThrow(p, valueFactory.createIRI(o), supplier);
    }

    public <T extends Throwable> Resource selectSubjectOrThrow(
            IRI p, Value o, Supplier<T> supplier) throws T {
        Collection<Resource> candidates = select(null, p, o).subjects();
        if (candidates.size() != 1) {
            throw supplier.get();
        }
        return candidates.iterator().next();
    }

    public Statements selectByGraph(String graph) {
        return selectByGraph(valueFactory.createIRI(graph));
    }

    public Statements selectByGraph(Resource graph) {
        Statements result = Statements.arrayList();
        collection.stream()
                .filter((st) -> Objects.equal(graph, st.getContext()))
                .forEach(result::add);
        return result;
    }

    public Statements selectByGraph(Statement statement) {
        return selectByGraph(statement.getContext());
    }

    public List<Value> selectList(Resource s, String p) {
        return selectList(s, valueFactory.createIRI(p));
    }

    public List<Value> selectList(Resource s, IRI p) {
        List<Value> result = new ArrayList<>();
        for (Statement statement : select(s, p, null)) {
            selectList(statement.getObject(), result);
        }
        return result;
    }

    public void selectList(Value value, List<Value> collector) {
        if (RDF.NIL.equals(value)) {
            return;
        }
        if (!value.isResource()) {
            collector.add(value);
            return;
        }
        for (Statement statement : collection) {
            if (!statement.getSubject().equals(value)) {
                continue;
            }
            IRI predicate = statement.getPredicate();
            if (RDF.FIRST.equals(predicate)) {
                collector.add(statement.getObject());
            } else if (RDF.REST.equals(predicate)) {
                selectList(statement.getObject(), collector);
            }
        }
    }

}
