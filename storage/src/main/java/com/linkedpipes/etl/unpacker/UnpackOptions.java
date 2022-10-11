package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe options that can be used to modify pipeline unpacking.
 */
class UnpackOptions implements Loadable {

    public static final String TYPE =
            "http://etl.linkedpipes.com/ontology/ExecutionOptions";

    public static class ComponentMapping   implements Loadable {

        private String source;

        private String target;

        @Override
        public Loadable load(String predicate, Value value) {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/mappingSource":
                    source = value.stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/mappingTarget":
                    target = value.stringValue();
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

    public static class ExecutionMapping implements Loadable {

        private String execution;

        private final List<ComponentMapping> mappings = new LinkedList<>();

        private final List<ComponentMapping> resumes = new LinkedList<>();

        @Override
        public Loadable load(String predicate, Value value) {
            switch (predicate) {
                case "http://etl.linkedpipes.com/ontology/execution":
                    execution = value.stringValue();
                    return null;
                case "http://etl.linkedpipes.com/ontology/mapping":
                    ComponentMapping mapping = new ComponentMapping();
                    mappings.add(mapping);
                    return mapping;
                case "http://etl.linkedpipes.com/ontology/resume":
                    ComponentMapping resume = new ComponentMapping();
                    resumes.add(resume);
                    return resume;
                default:
                    return null;
            }

        }

        public String getExecution() {
            return execution;
        }

        public List<ComponentMapping> getMappings() {
            return Collections.unmodifiableList(mappings);
        }

        public List<ComponentMapping> getResumes() {
            return Collections.unmodifiableList(resumes);
        }
    }

    private String runToComponent;

    private List<ExecutionMapping> executionMapping = new LinkedList<>();

    private boolean saveDebugData = true;

    private boolean deleteWorkingDirectory = false;

    private String executionIri;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    private String logLevel = "DEBUG";

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/runTo":
                runToComponent = value.stringValue();
                return null;
            case "http://etl.linkedpipes.com/ontology/executionMapping":
                final ExecutionMapping mapping = new ExecutionMapping();
                executionMapping.add(mapping);
                return mapping;
            case "http://linkedpipes.com/ontology/saveDebugData":
                if (value instanceof Literal literal) {
                    saveDebugData = literal.booleanValue();
                }
                return null;
            case "http://linkedpipes.com/ontology/deleteWorkingData":
                if (value instanceof Literal literal) {
                    deleteWorkingDirectory = literal.booleanValue();
                }
                return null;
            case LP_EXEC.HAS_EXECUTION:
                executionIri = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_LOG_POLICY:
                logPolicy = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_LOG_LEVEL:
                logLevel = value.stringValue();
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

    public String getLogLevel() {
        return logLevel;
    }

}
