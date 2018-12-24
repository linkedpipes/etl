package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe options that can be used to modify pipeline unpacking.
 */
class UnpackOptions implements Loadable {

    public static final String TYPE =
            "http://etl.linkedpipes.com/ontology/ExecutionOptions";

    public static class ComponentMapping
            implements Loadable {

        private String source;

        private String target;

        @Override
        public Loadable load(String predicate, BackendRdfValue value) {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/mappingSource":
                    source = value.asString();
                    break;
                case "http://etl.linkedpipes.com/ontology/mappingTarget":
                    target = value.asString();
                    break;
                default:
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
            implements Loadable {

        private String execution;

        private final List<ComponentMapping> components = new LinkedList<>();

        @Override
        public Loadable load(String predicate, BackendRdfValue value) {
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

    private String executionIri;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    @Override
    public Loadable load(String predicate, BackendRdfValue value)
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
            case LP_EXEC.HAS_EXECUTION:
                executionIri = value.asString();
                return null;
            case LP_PIPELINE.HAS_LOG_POLICY:
                logPolicy = value.asString();
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

    public String getExecutionIri() {
        return executionIri;
    }

    public String getLogPolicy() {
        return logPolicy;
    }

}
