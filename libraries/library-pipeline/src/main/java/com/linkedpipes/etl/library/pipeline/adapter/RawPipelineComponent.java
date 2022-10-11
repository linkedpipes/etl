package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;

public class RawPipelineComponent {

    /**
     * Component resource.
     */
    public Resource resource;

    /**
     * User given label.
     */
    public String label;

    /**
     * User given description.
     */
    public String description;

    /**
     * User given note.
     */
    public String note;

    /**
     * User given color.
     */
    public String color;

    /**
     * Position coordinates.
     */
    public Integer x;

    /**
     * Position coordinates.
     */
    public Integer y;

    /**
     * Template.
     */
    public Resource template;

    /**
     * True if execution of this component is disabled.
     */
    public boolean disabled = false;

    /**
     * Configuration without graph.
     */
    public Statements configuration;

    /**
     * Configuration graph.
     */
    public Resource configurationGraph;

}
