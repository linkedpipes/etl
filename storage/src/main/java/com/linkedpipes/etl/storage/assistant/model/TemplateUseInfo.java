package com.linkedpipes.etl.storage.assistant.model;

import org.eclipse.rdf4j.model.Resource;

import java.util.List;

public record TemplateUseInfo (
    /*
     * Identification of the template.
     */
    Resource resource,
    /*
     * Parent, not a plugin.
     */
    Resource template,
    /*
     * List of pipelines where the template is used.
     */
    List<PipelineInfo> pipelines
) {

}
