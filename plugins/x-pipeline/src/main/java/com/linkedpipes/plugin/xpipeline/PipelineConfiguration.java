package com.linkedpipes.plugin.xpipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = PipelineVocabulary.CONFIG_CLASS)
public class PipelineConfiguration {

    @RdfToPojo.Property(iri = PipelineVocabulary.INSTANCE)
    private String instance = null;

    @RdfToPojo.Property(iri = PipelineVocabulary.PIPELINE)
    private String pipeline = null;

    @RdfToPojo.Property(iri = PipelineVocabulary.SAVE_DEBUG_DATA)
    private boolean saveDebugData = true;

    @RdfToPojo.Property(iri = PipelineVocabulary.DELETE_WORKING_DATA)
    private boolean deleteWorkingDirectory = false;

    @RdfToPojo.Property(iri = PipelineVocabulary.LOG_POLICY)
    private String logPolicy = "Preserve";

    public PipelineConfiguration() {
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public boolean isSaveDebugData() {
        return saveDebugData;
    }

    public void setSaveDebugData(boolean saveDebugData) {
        this.saveDebugData = saveDebugData;
    }

    public boolean isDeleteWorkingDirectory() {
        return deleteWorkingDirectory;
    }

    public void setDeleteWorkingDirectory(boolean deleteWorkingDirectory) {
        this.deleteWorkingDirectory = deleteWorkingDirectory;
    }

    public String getLogPolicy() {
        return logPolicy;
    }

    public void setLogPolicy(String logPolicy) {
        this.logPolicy = logPolicy;
    }

}
