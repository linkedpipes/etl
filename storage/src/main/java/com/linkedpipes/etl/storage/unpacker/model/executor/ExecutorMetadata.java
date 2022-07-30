package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;

/**
 * For preserving backwards compatibility.
 */
public class ExecutorMetadata {

    private final String iri;

    private boolean deleteWorkingData;

    private String targetComponent;

    private String executionType;

    private boolean saveDebugData;

    private String logPolicy = LP_PIPELINE.LOG_PRESERVE;

    private String logLevel = "INFO";

    public ExecutorMetadata(String iri) {
        this.iri = iri;
    }

    public void write(StatementsBuilder builder) {
        builder.addType(iri, LP_PIPELINE.EXECUTION_METADATA);
        builder.add(iri, LP_PIPELINE.HAS_DELETE_WORKING, deleteWorkingData);
        builder.add(iri, LP_PIPELINE.HAS_SAVE_DEBUG_DATA, saveDebugData);
        builder.addIri(iri, LP_PIPELINE.HAS_LOG_POLICY, logPolicy);
        builder.add(iri, LP_PIPELINE.HAS_LOG_LEVEL, logLevel);
        if (targetComponent != null && !targetComponent.isEmpty()) {
            builder.addIri(iri, LP_EXEC.HAS_TARGET_COMPONENT, targetComponent);
        }
        builder.addIri(iri, LP_EXEC.HAS_PIPELINE_EXECUTION_TYPE, executionType);
    }

    public String getIri() {
        return iri;
    }

    public void setTargetComponent(String targetComponent) {
        this.targetComponent = targetComponent;
    }

    public void setDeleteWorkingData(boolean deleteWorkingData) {
        this.deleteWorkingData = deleteWorkingData;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public void setSaveDebugData(boolean saveDebugData) {
        this.saveDebugData = saveDebugData;
    }

    public void setLog(String policy, String level) {
        this.logPolicy = policy;
        this.logLevel = level;
    }

}
