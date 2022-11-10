package com.linkedpipes.etl.storage.distribution.model;

import org.eclipse.rdf4j.model.Resource;

public class ImportPipelineOptions {

    /**
     * Identify pipeline to import. If only one pipeline is imported
     * this can be null.
     */
    public Resource pipeline = null;

    /**
     * If set force the pipeline resource upon import.
     */
    public Resource targetResource = null;

    /**
     * Import pipeline.
     */
    public boolean importPipeline = false;

    /**
     * Keep whole pipeline URL.
     */
    public boolean keepPipelineUrl = false;

    /**
     * If true pipeline URL suffix is preserved.
     */
    public boolean keepPipelineSuffix = false;

    /**
     * If set should be used instead of a pipeline label.
     */
    public String targetLabel = null;

    public ImportPipelineOptions() {
    }

    public ImportPipelineOptions copyForPipeline(Resource pipeline) {
        ImportPipelineOptions result = new ImportPipelineOptions();
        result.pipeline = pipeline;
        result.targetResource = null;
        result.importPipeline = importPipeline;
        result.keepPipelineUrl = keepPipelineUrl;
        result.keepPipelineSuffix = keepPipelineSuffix;
        result.targetLabel = null;
        return result;
    }

}
