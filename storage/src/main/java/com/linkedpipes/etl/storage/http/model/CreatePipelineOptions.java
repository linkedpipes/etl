package com.linkedpipes.etl.storage.http.model;

import org.eclipse.rdf4j.model.Resource;

public class CreatePipelineOptions {

    /**
     * If set force the pipeline resource.
     */
    public Resource targetResource = null;

    /**
     * If set should be used instead of a pipeline label.
     */
    public String targetLabel = null;

}
