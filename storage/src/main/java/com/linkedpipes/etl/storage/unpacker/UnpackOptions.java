package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe options that can be used to modify pipeline unpacking.
 *
 * @author Petr Škoda
 */
class UnpackOptions implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        final ValueFactory fv = SimpleValueFactory.getInstance();
        TYPE = fv.createIRI(
                "http://etl.linkedpipes.com/ontology/ExecutionOptions");
    }

    public static class ComponentMapping implements PojoLoader.Loadable {

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

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }
    }

    public static class ExecutionMapping implements PojoLoader.Loadable {

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

        public String getExecution() {
            return execution;
        }

        public List<ComponentMapping> getComponents() {
            return Collections.unmodifiableList(components);
        }
    }

    private String runToComponent;

    private List<ExecutionMapping> executionMapping = new LinkedList<>();

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

}
