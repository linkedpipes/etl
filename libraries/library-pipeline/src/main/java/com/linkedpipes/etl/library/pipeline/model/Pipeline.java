package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record Pipeline(
        /*
         * Resource.
         */
        Resource resource,
        /*
         * Time when pipeline was created on this instance. May be
         * by import.
         */
        LocalDateTime created,
        /*
         * Last pipeline update time.
         */
        LocalDateTime lastUpdate,
        /*
         * Pipeline label, only one is allowed.
         */
        String label,
        /*
         * Version.
         */
        int version,
        /*
         * Pipeline note.
         */
        String note,
        /*
         * Pipeline tags.
         */
        List<String> tags,
        /*
         * Execution profile.
         */
        PipelineExecutionProfile executionProfile,
        /*
         * List of components.
         */
        List<PipelineComponent> components,
        /*
         * List of data connections.
         */
        List<PipelineDataFlow> dataFlows,
        /*
         * List of control connections.
         */
        List<PipelineControlFlow> controlFlows
) {

    public static final Integer VERSION = 5;

    private static final String IRI_INFIX = "/resources/pipelines/";

    public Pipeline {
        tags = Collections.unmodifiableList(tags);
        components = Collections.unmodifiableList(components);
        dataFlows = Collections.unmodifiableList(dataFlows);
        controlFlows = Collections.unmodifiableList(controlFlows);
    }

    public Pipeline(Pipeline other) {
        this(
                other.resource,
                other.created,
                other.lastUpdate,
                other.label,
                other.version,
                other.note,
                other.tags,
                other.executionProfile,
                other.components,
                other.dataFlows,
                other.controlFlows
        );
    }

    public static Resource createResource(String domain, String suffix) {
        String result = domain + IRI_INFIX + suffix;
        return SimpleValueFactory.getInstance().createIRI(result);
    }

}
