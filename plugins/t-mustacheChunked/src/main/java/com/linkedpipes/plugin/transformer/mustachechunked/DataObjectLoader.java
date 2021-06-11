package com.linkedpipes.plugin.transformer.mustachechunked;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class DataObjectLoader {

    protected final IRI objectClass;

    protected final boolean includeFirstFlag;

    protected final EscapeForJson escapeForJson;

    /**
     * Holds object metadata.
     */
    protected Map<Resource, ObjectDataHolder> objectsInfo = new HashMap<>();

    /**
     * For each object we store it's properties.
     */
    protected Map<Resource, Map<IRI, List<Value>>> objects = new HashMap<>();

    /**
     * Cache for building object to handle cycles, the cache store
     * only values loaded from the statements, not properties added
     * based on the object info, such as first.
     */
    protected Map<Resource, Map<String, Object>> buildCache = new HashMap<>();

    public DataObjectLoader(MustacheConfiguration configuration) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        objectClass = valueFactory.createIRI(configuration.getResourceClass());
        includeFirstFlag = configuration.isAddFirstToCollection();
        if (configuration.isEscapeForJson()) {
            escapeForJson = new EscapeForJson();
        } else {
            escapeForJson = null;
        }
    }

    public List<ObjectDataHolder> loadData(Collection<Statement> statements) {
        clearLocalState();
        parseStatements(statements);
        List<ObjectDataHolder> result = collectObjects();
        if (includeFirstFlag) {
            addFirstFlags(result);
        }
        clearLocalState();
        return result;
    }

    protected void clearLocalState() {
        objectsInfo.clear();
        objects.clear();
        buildCache.clear();
    }

    protected void parseStatements(Collection<Statement> statements) {
        for (Statement statement : statements) {
            parseStatement(statement);
        }
    }

    protected void parseStatement(Statement st) {
        Resource resource = st.getSubject();
        switch (st.getPredicate().stringValue()) {
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                if (st.getObject().equals(objectClass)) {
                    getOrCreate(resource).output = true;
                }
                addStatementToObject(st);
                break;
            case MustacheVocabulary.HAS_ORDER:
                Literal literal = (Literal) st.getObject();
                getOrCreate(resource).order = literal.intValue();
                break;
            case MustacheVocabulary.HAS_FILE_NAME:
                getOrCreate(resource).fileName = st.getObject().stringValue();
                break;
            default:
                addStatementToObject(st);
                break;
        }
    }

    protected ObjectDataHolder getOrCreate(Resource resource) {
        if (!objectsInfo.containsKey(resource)) {
            objectsInfo.put(resource, new ObjectDataHolder());
        }
        return objectsInfo.get(resource);
    }

    protected void addStatementToObject(Statement statement) {
        Resource subject = statement.getSubject();
        if (!objects.containsKey(subject)) {
            objects.put(subject, new HashMap<>());
        }
        IRI predicate = statement.getPredicate();
        Map<IRI, List<Value>> resource = objects.get(subject);
        if (!resource.containsKey(predicate)) {
            resource.put(predicate, new LinkedList<>());
        }
        List<Value> values = resource.get(predicate);
        values.add(statement.getObject());
    }

    protected List<ObjectDataHolder> collectObjects() {
        List<ObjectDataHolder> result = new ArrayList<>();
        for (var entry : objectsInfo.entrySet()) {
            ObjectDataHolder object = entry.getValue();
            if (!object.output) {
                continue;
            }
            object.data = buildObject(entry.getKey());
            result.add(object);
        }
        return result;
    }

    protected Map<String, Object> buildObject(Resource resource) {
        Map<IRI, List<Value>> objectData = objects.get(resource);
        if (objectData == null || objectData.isEmpty()) {
            return buildEmptyObject(resource);
        }
        return buildNonEmptyObject(resource, objectData);
    }

    protected Map<String, Object> buildEmptyObject(Resource resource) {
        Map<String, Object> result = new HashMap<>();
        result.put("@id", escapeString(resource.stringValue()));
        return result;
    }

    protected Map<String, Object> buildNonEmptyObject(
            Resource resource, Map<IRI, List<Value>> objectData) {
        if (buildCache.containsKey(resource)) {
            return buildCache.get(resource);
        }
        Map<String, Object> result = new HashMap<>();
        buildCache.put(resource, result);
        //
        result.put("@id", escapeString(resource.stringValue()));
        for (Map.Entry<IRI, List<Value>> entry : objectData.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            String key = escapeString(entry.getKey().stringValue());
            Object value = buildObjectProperty(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    protected String escapeString(String string) {
        if (escapeForJson == null) {
            return string;
        }
        return escapeForJson.escape(string);
    }

    protected Object buildObjectProperty(List<Value> values) {
        // We assume that all the data are the same in the array,
        // so we decide based on the first one and them apply the
        // transformation for all of them.
        boolean isResource = values.get(0) instanceof Resource;
        List<Object> transformed = values.stream()
                .sorted(this::compareValues)
                .map(value -> {
                    if (isResource) {
                        return transformResource((Resource) value);
                    } else {
                        return transformValue(value);
                    }
                }).collect(Collectors.toList());
        if (transformed.size() == 1) {
            return transformed.get(0);
        } else {
            return transformed;
        }
    }

    protected int compareValues(Value left, Value right) {
        ObjectDataHolder leftMeta = objectsInfo.get(left);
        ObjectDataHolder rightMeta = objectsInfo.get(right);
        // We need to have the metadata.
        if (leftMeta == null || rightMeta == null) {
            return 0;
        }
        // We need to have the values.
        if (leftMeta.order == null || rightMeta.order == null) {
            return 0;
        }
        return leftMeta.order.compareTo(rightMeta.order);
    }

    protected Map<String, Object> transformResource(Resource resource) {
        return buildObject(resource);
    }

    protected Object transformValue(Value value) {
        if (value instanceof Literal) {
            Literal literal = (Literal) value;
            switch (literal.getDatatype().stringValue()) {
                case "http://www.w3.org/2001/XMLSchema#boolean":
                    return literal.booleanValue();
            }
        }
        return escapeString(value.stringValue());
    }

    protected void addFirstFlags(List<ObjectDataHolder> list) {
        for (ObjectDataHolder item : list) {
            item.data = addFirstFlagsForObject(new HashSet<>(), item.data);
        }
    }

    protected Object addFirstFlagsForObject(Set<Object> visited, Object data) {
        if (data instanceof Map) {
            return addFirstFlagsForMap(visited, (Map<String, Object>) data);
        }
        if (data instanceof List) {
            return addFirstFlagsForList(visited, (List<Object>) data);
        }
        return data;
    }

    /**
     * For maps we just need to search all properties recursively.
     */
    protected Map<String, Object> addFirstFlagsForMap(
            Set<Object> visited, Map<String, Object> map) {
        Object identifier = map.get("@id");
        if (visited.contains(identifier)) {
            return map;
        }
        Set<Object> newVisited = new HashSet<>(visited);
        newVisited.add(identifier);
        //
        for (var entry : map.entrySet()) {
            Object value = addFirstFlagsForObject(newVisited, entry.getValue());
            map.put(entry.getKey(), value);
        }
        return map;
    }

    protected List<Object> addFirstFlagsForList(
            Set<Object> visited, List<Object> list) {
        if (list.isEmpty()) {
            return list;
        }
        // We add first to the first flag to the first one.
        Object first = setFirstToFirstObject(list.get(0));
        addFirstFlagsForObject(visited, first);
        list.set(0, first);
        // Then we just iterate other.
        for (int index = 1; index < list.size(); ++index) {
            list.set(index, addFirstFlagsForObject(visited, list.get(index)));
        }
        return list;
    }

    protected Object setFirstToFirstObject(Object input) {
        if (!(input instanceof Map)) {
            return input;
        }
        Map<String, Object> map = (Map<String, Object>) input;
        Map<String, Object> result = new HashMap<>(map);
        result.put(MustacheVocabulary.HAS_IS_FIRST, true);
        return result;
    }


}
