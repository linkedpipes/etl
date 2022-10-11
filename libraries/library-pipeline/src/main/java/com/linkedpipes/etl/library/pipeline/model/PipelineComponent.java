package com.linkedpipes.etl.library.pipeline.model;

import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

public record PipelineComponent(
        /*
         * Component resource.
         */
        Resource resource,
        /*
         * User given label.
         */
        String label,
        /*
         * User given description.
         */
        String description,
        /*
         * User given note.
         */
        String note,
        /*
         * User given color.
         */
        String color,
        /*
         * Position coordinates.
         */
        Integer x,
        /*
         * Position coordinates.
         */
        Integer y,
        /*
         * Template.
         */
        Resource template,
        /*
         * True if execution of this component is disabled.
         */
        boolean disabled,
        /*
         * Configuration stored without graph.
         */
        Statements configuration,
        /*
         * Configuration without graph.
         */
        Resource configurationGraph
) {

    public PipelineComponent {
        configuration = Statements.readOnly(configuration);
    }

    public PipelineComponent(PipelineComponent other) {
        this(
                other.resource,
                other.label,
                other.description,
                other.note,
                other.color,
                other.x,
                other.y,
                other.template,
                other.disabled,
                other.configuration,
                other.configurationGraph
        );
    }

}
