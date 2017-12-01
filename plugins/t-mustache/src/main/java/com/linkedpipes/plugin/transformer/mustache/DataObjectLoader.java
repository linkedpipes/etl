package com.linkedpipes.plugin.transformer.mustache;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.*;

class DataObjectLoader {

    private final Repository repository;

    private final IRI graph;

    private final IRI objectClass;

    private final boolean includeFirstFlag;

    private Map<Resource, ObjectDataHolder> objectsInfo = new HashMap<>();

    private Map<Resource, Map<IRI, List<Value>>> objects = new HashMap<>();

    public DataObjectLoader(SingleGraphDataUnit dataUnit,
            MustacheConfiguration configuration) {
        repository = dataUnit.getRepository();
        graph = dataUnit.getReadGraph();
        objectClass = SimpleValueFactory.getInstance().createIRI(
                configuration.getResourceClass());
        includeFirstFlag = configuration.isAddFirstToCollection();
    }

    public List<ObjectDataHolder> loadData() throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            try (RepositoryResult<Statement> result
                         = connection.getStatements(null, null, null, graph)) {
                parseStatements(result);
            }
        }
        return buildObjectHolderList();
    }

    private void parseStatements(RepositoryResult<Statement> statements) {
        while (statements.hasNext()) {
            parseStatement(statements.next());
        }
    }

    private void parseStatement(Statement st) {
        Resource resource = st.getSubject();
        switch (st.getPredicate().stringValue()) {
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                if (st.getObject().equals(objectClass)) {
                    getOrCreate(resource).output = true;
                }
                break;
            case MustacheVocabulary.HAS_ORDER:
                Literal literal = (Literal) st.getObject();
                getOrCreate(resource).order = literal.intValue();
                break;
            case MustacheVocabulary.HAS_FILE_NAME:
                getOrCreate(resource).fileName = st.getObject().stringValue();
                break;
            default:
                addStatement(st);
                break;
        }
    }

    private ObjectDataHolder getOrCreate(Resource resource) {
        if (!objectsInfo.containsKey(resource)) {
            objectsInfo.put(resource, new ObjectDataHolder());
        }
        return objectsInfo.get(resource);
    }

    private void addStatement(Statement statement) {
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

    private List<ObjectDataHolder> buildObjectHolderList() {
        List<ObjectDataHolder> objectsOutput = new LinkedList<>();
        for (Resource resource : objectsInfo.keySet()) {
            ObjectDataHolder object = objectsInfo.get(resource);
            if (!object.output) {
                continue;
            }
            object.data = createDataObject(resource);
            objectsOutput.add(object);
        }
        return objectsOutput;
    }

    private Map<String, Object> createDataObject(Resource resource) {
        Map<IRI, List<Value>> data = objects.get(resource);
        if (data == null || data.isEmpty()) {
            return buildEmptyDataObject(resource);
        }
        return buildNonEmptyDataObject(resource, data);
    }

    private Map<String, Object> buildEmptyDataObject(Resource resource) {
        Map<String, Object> result = new HashMap<>();
        result.put("@id", resource.stringValue());
        return result;
    }

    private Map<String, Object> buildNonEmptyDataObject(Resource resource,
            Map<IRI, List<Value>> objectData) {
        Map<String, Object> result = new HashMap<>();
        result.put("@id", resource.stringValue());
        for (Map.Entry<IRI, List<Value>> entry : objectData.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (entry.getValue().get(0) instanceof Resource) {
                result.putAll(transformResource(entry));
            } else {
                result.putAll(transformValue(entry));
            }
        }
        return result;
    }

    private Map<String, Object> transformResource(
            Map.Entry<IRI, List<Value>> entry) {
        Map<String, Object> output = new HashMap<>();
        if (entry.getValue().size() == 1) {
            Resource resource = (Resource) entry.getValue().get(0);
            Map<String, Object> resourceObject = createDataObject(resource);
            if (includeFirstFlag) {
                addFirstFlag(resourceObject);
            }
            output.put(entry.getKey().stringValue(), resourceObject);
        } else {
            output.put(entry.getKey().stringValue(),
                    transformResourceList(entry));
        }
        return output;
    }

    private Object transformResourceList(Map.Entry<IRI, List<Value>> entry) {
        List<Value> values = new LinkedList<>();
        values.addAll(entry.getValue());
        sortValues(values);
        // Load new objects.
        List<Object> outputData = new ArrayList<>(entry.getValue().size());
        for (Value value : values) {
            Object dataObject = createDataObject((Resource) value);
            outputData.add(dataObject);
        }
        if (includeFirstFlag) {
            addFirstFlagToList(outputData);
        }
        return outputData;
    }

    private void sortValues(List<Value> values) {
        values.sort((Value left, Value right) -> {
            ObjectDataHolder leftMeta = objectsInfo.get(left);
            ObjectDataHolder rightMeta = objectsInfo.get(right);
            if (leftMeta == null || rightMeta == null
                    || leftMeta.order == null || rightMeta.order == null) {
                return 0;
            } else if (leftMeta.order < rightMeta.order) {
                return -1;
            } else if (leftMeta.order > rightMeta.order) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    private Map<String, Object> transformValue(
            Map.Entry<IRI, List<Value>> entry) {
        String predicate = entry.getKey().stringValue();
        if (entry.getValue().size() == 1) {
            Map<String, Object> output = new HashMap<>();
            output.put(predicate, getValue(entry.getValue().get(0)));
            return output;
        } else {
            Map<String, Object> output = new HashMap<>();
            List<Object> newData = new ArrayList<>(entry.getValue().size());
            for (Value value : entry.getValue()) {
                newData.add(getValue(value));
            }
            output.put(predicate, newData);
            return output;
        }
    }

    private static Object getValue(Value value) {
        if (value instanceof Literal) {
            Literal literal = (Literal) value;
            switch (literal.getDatatype().stringValue()) {
                case "http://www.w3.org/2001/XMLSchema#boolean":
                    return literal.booleanValue();
            }
        }
        return value.stringValue();
    }

    private void addFirstFlag(Object object) {
        if (object instanceof Map) {
            ((Map) object).put(MustacheVocabulary.HAS_IS_FIRST, true);
        }
    }

    private void addFirstFlagToList(List<Object> objects) {
        boolean isFirst = true;
        for (Object object : objects) {
            if (object instanceof Map) {
                ((Map) object).put(MustacheVocabulary.HAS_IS_FIRST, isFirst);
                isFirst = false;
            }
        }
    }


}
