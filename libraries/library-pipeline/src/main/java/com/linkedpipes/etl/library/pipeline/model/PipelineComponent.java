package com.linkedpipes.etl.library.pipeline.model;

import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

public record PipelineComponent(
        Resource resource,
        Literal label,
        Literal description,
        Literal note,
        Value color,
        Literal xPosition,
        Literal yPosition,
        Resource template,
        Literal disabled,
        IRI configurationGraph,
        /*
         * Configuration stored without graph.
         */
        Statements configuration
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
                other.xPosition,
                other.yPosition,
                other.template,
                other.disabled,
                other.configurationGraph,
                other.configuration
        );
    }

}
