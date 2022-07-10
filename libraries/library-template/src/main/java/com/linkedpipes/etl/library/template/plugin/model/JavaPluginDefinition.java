package com.linkedpipes.etl.library.template.plugin.model;

import org.eclipse.rdf4j.model.IRI;

import java.util.List;

public record JavaPluginDefinition(
        /*
         * Plugin identification.
         */
        IRI plugin,
        /*
         * Directories with template plugins.
         */
        List<String> directories
) {
}
