package com.linkedpipes.plugin.transformer.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public final class MustacheComponent implements Component.Sequential {

    /**
     * Used to hold metadata about loaded objects.
     */
    private static class ObjectMetadata {

        /**
         * If true object is used to generate output.
         */
        boolean output;

        /**
         * Used for object ordering.
         */
        Integer order = null;

        /**
         * If {@link #output} is true then specify name of the output file.
         * If null some name is generated and used.
         */
        String fileName;

        /**
         * If {@link #output} is true then contains data.
         */
        Object data;

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(MustacheComponent.class);

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit input;

    @Component.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public MustacheConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        // Prepare template
        final String template
                = UpdateQuery.expandPrefixes(configuration.getTemplate());
        LOG.info("Input template:\n{}\nUsed template:\n{}",
                configuration.getTemplate(),
                template);
        //
        final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        final Mustache mustache = mustacheFactory.compile(
                new StringReader(template), "template");
        final Collection<ObjectMetadata> data = loadData();
        // If there is no input add an empty object.
        // https://github.com/linkedpipes/etl/issues/152
        if (data.isEmpty()) {
            final ObjectMetadata emptyOutput = new ObjectMetadata();
            emptyOutput.output = true;
            data.add(emptyOutput);
        }
        progressReport.start(data.size());
        Integer counter = 0;
        for (ObjectMetadata object : data) {
            final String fileName;
            counter += 1;
            if (object.fileName != null) {
                fileName = object.fileName;
            } else {
                fileName = "output_" + counter;
            }
            final File outputFile = output.createFile(fileName).toFile();
            try (OutputStreamWriter outputStream = new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF8")) {
                mustache.execute(outputStream, object.data).flush();
            } catch (IOException ex) {
                throw exceptionFactory.failed("Can't write output file.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    /**
     * Load RDF data.
     *
     * @return
     * @throws NonRecoverableException
     */
    private Collection<ObjectMetadata> loadData() throws LpException {
        final Map<Resource, ObjectMetadata> objectsInfo = new HashMap<>();
        final Map<Resource, Map<IRI, List<Value>>> objects = new HashMap<>();
        // Load basic informations about objects.
        try (RepositoryConnection connection
                = input.getRepository().getConnection()) {
            try (RepositoryResult<Statement> result = connection.getStatements(
                    null, null, null, input.getGraph())) {
                while (result.hasNext()) {
                    final Statement st = result.next();
                    // Check for interest properties.
                    final Resource resource = st.getSubject();
                    switch (st.getPredicate().stringValue()) {
                        case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                            if (st.getObject().stringValue().equals(
                                    configuration.getResourceClass())) {
                                getObject(resource, objectsInfo).output = true;
                            }
                            break;
                        case MustacheVocabulary.HAS_ORDER:
                            getObject(resource, objectsInfo).order
                                    = ((Literal) st.getObject()).intValue();
                            break;
                        case MustacheVocabulary.HAS_FILE_NAME:
                            getObject(resource, objectsInfo).fileName
                                    = st.getObject().stringValue();
                            break;
                        default:
                            addStatement(resource, st.getPredicate(),
                                    st.getObject(), objects);
                            break;
                    }
                }
            }
        }
        // Create output.
        final List<ObjectMetadata> objectsOutput = new LinkedList<>();
        for (Resource resource : objectsInfo.keySet()) {
            final ObjectMetadata object = objectsInfo.get(resource);
            if (!object.output) {
                continue;
            }
            object.data = createDataObject(resource, objectsInfo, objects);
            objectsOutput.add(object);
        }
        return objectsOutput;
    }

    private static Object createDataObject(Resource resource,
            Map<Resource, ObjectMetadata> objectsInfo,
            Map<Resource, Map<IRI, List<Value>>> objects) {
        final Map<IRI, List<Value>> data = objects.get(resource);
        if (data == null || data.isEmpty()) {
            // There are no data, return string representaion or IRI..
            return resource.stringValue();
        }
        final Map<String, Object> result = new HashMap<>();
        // Add @id to the resource.
        result.put("@id", resource);
        // Add data entries.
        for (Map.Entry<IRI, List<Value>> entry : data.entrySet()) {
            // Check type.
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (entry.getValue().get(0) instanceof Resource) {
                // Contains references.
                if (entry.getValue().size() == 1) {
                    // A simple reference.
                    result.put(entry.getKey().stringValue(),
                            createDataObject((Resource) entry.getValue().get(0),
                                    objectsInfo, objects));
                } else {
                    // Sort the list with references.
                    final List<Value> values = new LinkedList<>();
                    values.addAll(entry.getValue());
                    values.sort((Value left, Value right) -> {
                        final ObjectMetadata leftMeta
                                = objectsInfo.get((Resource) left);
                        final ObjectMetadata rightMeta
                                = objectsInfo.get((Resource) right);
                        if (leftMeta == null || rightMeta == null
                                || leftMeta.order == null
                                || rightMeta.order == null) {
                            return 0;
                        } else if (leftMeta.order < rightMeta.order) {
                            return -1;
                        } else if (leftMeta.order > rightMeta.order) {
                            return 1;
                        } else {
                            return 0;
                        }
                    });
                    // Load new objects.
                    final List<Object> newData
                            = new ArrayList<>(entry.getValue().size());
                    for (Value value : values) {
                        newData.add(createDataObject((Resource) value,
                                objectsInfo, objects));
                    }
                    result.put(entry.getKey().stringValue(), newData);
                }
            } else // Values.
             if (entry.getValue().size() == 1) {
                    result.put(entry.getKey().stringValue(),
                            getValue(entry.getValue().get(0)));
                } else {
                    final List<Object> newData
                            = new ArrayList<>(entry.getValue().size());
                    for (Value value : entry.getValue()) {
                        newData.add(getValue(value));
                    }
                    result.put(entry.getKey().stringValue(), newData);
                }
        }
        return result;
    }

    private static Object getValue(Value value) {
        if (value instanceof Literal) {
            final Literal literal = (Literal) value;
            switch (literal.getDatatype().stringValue()) {
                case "http://www.w3.org/2001/XMLSchema#boolean":
                    return literal.booleanValue();
            }
        }
        return value.stringValue();
    }

    private static ObjectMetadata getObject(Resource resource,
            Map<Resource, ObjectMetadata> data) {
        if (!data.containsKey(resource)) {
            data.put(resource, new ObjectMetadata());
        }
        return data.get(resource);
    }

    /**
     * Add statement to the data object.
     *
     * @param subject
     * @param predicate
     * @param object
     * @param data
     */
    private static void addStatement(Resource subject, IRI predicate,
            Value object, Map<Resource, Map<IRI, List<Value>>> data) {
        if (!data.containsKey(subject)) {
            data.put(subject, new HashMap<>());
        }
        final Map<IRI, List<Value>> resource = data.get(subject);
        if (!resource.containsKey(predicate)) {
            resource.put(predicate, new LinkedList<>());
        }
        final List<Value> values = resource.get(predicate);
        values.add(object);
    }

}
