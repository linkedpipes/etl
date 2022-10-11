package com.linkedpipes.etl.library.template.reference.model;

import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.Resource;

import java.util.Collections;
import java.util.List;

/**
 * Full definition of a reference template data structure.
 */
public record ReferenceTemplate(
        /*
         * Resource.
         */
        Resource resource,
        /*
         * Reference version.
         */
        int version,
        /*
         * Parent template.
         */
        Resource template,
        /*
         * IRI of the top template, i.e. the plugin template.
         */
        Resource plugin,
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
         * Can be null.
         */
        String color,
        /*
         * List of tags assigned to given template.
         */
        List<String> tags,
        /*
         * When imported the original IRI is saved here, this allows us to
         * track a single template among instances.
         */
        Resource knownAs,
        /*
         * Configuration in form of RDF as there is no fixed structure.
         * Stored without graph.
         */
        Statements configuration,
        /*
         * Graph the configuration is stored in.
         */
        Resource configurationGraph
) {

    public ReferenceTemplate {
        tags = Collections.unmodifiableList(tags);
        configuration = Statements.readOnly(configuration);
    }

}
