package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineControlFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import com.linkedpipes.etl.library.pipeline.model.PipelineVertex;
import org.eclipse.rdf4j.model.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Version less pipeline.
 */
public class RawPipeline {

    /**
     * Resource.
     */
    public Resource resource;

    /**
     * Version.
     */
    public int version;

    /**
     * Pipeline identification on this instance.
     */
    public Resource publicResource;

    /**
     * Time when pipeline was created on this instance. May be
     * by import.
     */
    public LocalDateTime created;

    /**
     * Last pipeline update time.
     */
    public LocalDateTime lastUpdate;

    /**
     * Pipeline label; only one is allowed.
     */
    public String label;

    /**
     * Pipeline note.
     */
    public String note;

    /**
     * Pipeline tags.
     */
    public final List<String> tags = new ArrayList<>();

    /**
     * Execution profile.
     */
    public RawPipelineExecutionProfile executionProfile =
            new RawPipelineExecutionProfile();

    /**
     * List of components.
     */
    public final List<RawPipelineComponent> components = new ArrayList<>();

    /**
     * List of data connections.
     */
    public final List<RawPipelineDataFlow> dataFlows = new ArrayList<>();

    /**
     * List of control connections.
     */
    public final List<RawPipelineControlFlow> controlFlows = new ArrayList<>();

    public Pipeline toPipeline() {
        return new Pipeline(
                resource, publicResource,
                created, lastUpdate,
                label, version, note, tags,
                new PipelineExecutionProfile(
                        executionProfile.resource,
                        executionProfile.rdfRepositoryPolicy,
                        executionProfile.rdfRepositoryType,
                        executionProfile.logRetentionPolicy,
                        executionProfile.logRetentionPolicy,
                        executionProfile.failedExecutionLimit,
                        executionProfile.successfulExecutionLimit
                ),
                components.stream().map(
                        item -> new PipelineComponent(
                                item.resource,
                                item.label,
                                item.description,
                                item.note,
                                item.color,
                                item.x, item.y,
                                item.template,
                                item.disabled,
                                item.configuration,
                                item.configurationGraph
                        )
                ).toList(),
                dataFlows.stream().map(
                        item -> new PipelineDataFlow(
                                item.resource,
                                item.source,
                                item.sourceBinding,
                                item.target,
                                item.targetBinding,
                                item.vertices.stream().map(
                                        vertex -> new PipelineVertex(
                                                vertex.resource,
                                                vertex.order,
                                                vertex.x,
                                                vertex.y)
                                ).toList()
                        )
                ).toList(),
                controlFlows.stream().map(
                        item -> new PipelineControlFlow(
                                item.resource,
                                item.source,
                                item.target,
                                item.vertices.stream().map(
                                        vertex -> new PipelineVertex(
                                                vertex.resource,
                                                vertex.order,
                                                vertex.x,
                                                vertex.y)
                                ).toList())
                ).toList());
    }

}


