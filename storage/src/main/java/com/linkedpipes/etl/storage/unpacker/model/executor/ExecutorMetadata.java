package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

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

    public ExecutorMetadata(String iri) {
        this.iri = iri;
    }

    public void write(TripleWriter writer) {
        writer.iri(iri, RDF.TYPE, LP_PIPELINE.EXECUTION_METADATA);
        writer.bool(iri, LP_PIPELINE.HAS_DELETE_WORKING, deleteWorkingData);
        writer.bool(iri, LP_PIPELINE.HAS_SAVE_DEBUG_DATA, saveDebugData);
        writer.iri(iri, LP_PIPELINE.HAS_LOG_POLICY, logPolicy);
        if (targetComponent != null && !targetComponent.isEmpty()) {
            writer.iri(iri, LP_EXEC.HAS_TARGET_COMPONENT, targetComponent);
        }
        writer.iri(iri, LP_EXEC.HAS_PIPELINE_EXECUTION_TYPE, executionType);
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

    public void setLogPolicy(String logPolicy) {
        this.logPolicy = logPolicy;
    }

}
