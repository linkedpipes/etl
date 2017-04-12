package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe options that can be used to modify pipeline unpacking.
 */
class UnpackOptions implements PojoLoader.Loadable, Loadable {

    public static final IRI TYPE;

    static {
        final ValueFactory fv = SimpleValueFactory.getInstance();
        TYPE = fv.createIRI(
                "http://etl.linkedpipes.com/ontology/ExecutionOptions");
    }

    public static class ComponentMapping
            implements PojoLoader.Loadable, Loadable {

        private String source;

        private String target;

        @Override
        public PojoLoader.Loadable load(String predicate, Value value)
                throws PojoLoader.CantLoadException {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/mappingSource":
                    source = value.stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/mappingTarget":
                    target = value.stringValue();
                    break;
            }
            return null;
        }

        @Override
        public Loadable load(String predicate, RdfValue value)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/mappingSource":
                    source = value.asString();
                    break;
                case "http://etl.linkedpipes.com/ontology/mappingTarget":
                    target = value.asString();
                    break;
            }
            return null;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }
    }

    public static class ExecutionMapping
            implements PojoLoader.Loadable, Loadable {

        private String execution;

        private final List<ComponentMapping> components = new LinkedList<>();

        @Override
        public PojoLoader.Loadable load(String predicate, Value value)
                throws PojoLoader.CantLoadException {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/execution":
                    execution = value.stringValue();
                    return null;
                case "http://etl.linkedpipes.com/ontology/mapping":
                    final ComponentMapping mapping = new ComponentMapping();
                    components.add(mapping);
                    return mapping;
                default:
                    return null;
            }
        }

        @Override
        public Loadable load(String predicate, RdfValue value)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/execution":
                    execution = value.asString();
                    return null;
                case "http://etl.linkedpipes.com/ontology/mapping":
                    final ComponentMapping mapping = new ComponentMapping();
                    components.add(mapping);
                    return mapping;
                default:
                    return null;
            }

        }

        public String getExecution() {
            return execution;
        }

        public List<ComponentMapping> getComponents() {
            return Collections.unmodifiableList(components);
        }
    }

    private String runToComponent;

    private List<ExecutionMapping> executionMapping = new LinkedList<>();

    private boolean saveDebugData = true;

    private boolean deleteWorkingDirectory = false;

    @Override
    public PojoLoader.Loadable load(String predicate, Value value)
            throws PojoLoader.CantLoadException {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/runTo":
                runToComponent = value.stringValue();
                return null;
            case "http://etl.linkedpipes.com/ontology/executionMapping":
                final ExecutionMapping mapping = new ExecutionMapping();
                executionMapping.add(mapping);
                return mapping;
            case "http://linkedpipes.com/ontology/saveDebugData":
                saveDebugData = "true".equals(
                        value.stringValue().toLowerCase());
                return null;
            case "http://linkedpipes.com/ontology/deleteWorkingData":
                deleteWorkingDirectory = "true".equals(
                        value.stringValue().toLowerCase());
                return null;
            default:
                return null;
        }
    }

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/runTo":
                runToComponent = value.asString();
                return null;
            case "http://etl.linkedpipes.com/ontology/executionMapping":
                final ExecutionMapping mapping = new ExecutionMapping();
                executionMapping.add(mapping);
                return mapping;
            case "http://linkedpipes.com/ontology/saveDebugData":
                saveDebugData = value.asBoolean();
                return null;
            case "http://linkedpipes.com/ontology/deleteWorkingData":
                deleteWorkingDirectory = value.asBoolean();
                return null;
            default:
                return null;
        }
    }

    public String getRunToComponent() {
        return runToComponent;
    }

    public List<ExecutionMapping> getExecutionMapping() {
        return Collections.unmodifiableList(executionMapping);
    }

    public boolean isSaveDebugData() {
        return saveDebugData;
    }

    public boolean isDeleteWorkingDirectory() {
        return deleteWorkingDirectory;
    }
}
