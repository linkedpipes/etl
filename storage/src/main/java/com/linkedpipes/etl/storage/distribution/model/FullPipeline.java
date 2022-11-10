package com.linkedpipes.etl.storage.distribution.model;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;

import java.util.List;

/**
 * Full representation of a pipeline.
 */
public record FullPipeline(
        Pipeline pipeline,
        List<ReferenceTemplate> templates
) {

}
