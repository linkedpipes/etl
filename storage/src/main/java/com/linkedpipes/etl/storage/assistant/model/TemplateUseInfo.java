package com.linkedpipes.etl.storage.assistant.model;

import java.util.List;
import org.eclipse.rdf4j.model.Resource;

public record TemplateUseInfo(
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
        List<PipelineInfo> pipelines) {}
