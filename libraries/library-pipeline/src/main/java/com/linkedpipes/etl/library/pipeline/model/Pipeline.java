package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record Pipeline(
        Resource resource,
        /*
         * Pipeline identification on this instance.
         */
        Resource publicResource,
        /*
         * Time when pipeline was created on this instance. May be
         * by import.
         */
        LocalDateTime created,
        LocalDateTime lastUpdate,
        Literal label,
        Literal version,
        Literal note,
        List<Literal> tags,
        PipelineExecutionProfile executionProfile,
        List<PipelineComponent> components,
        List<PipelineConnection> connections
) {

    public Pipeline {
        tags = Collections.unmodifiableList(tags);
        components = Collections.unmodifiableList(components);
        connections = Collections.unmodifiableList(connections);
    }

    public Pipeline(Pipeline other) {
        this(
                other.resource,
                other.publicResource,
                other.created,
                other.lastUpdate,
                other.label,
                other.version,
                other.note,
                other.tags,
                other.executionProfile,
                other.components,
                other.connections
        );
    }

}
