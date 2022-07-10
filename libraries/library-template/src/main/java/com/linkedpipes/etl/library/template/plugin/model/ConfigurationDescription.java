package com.linkedpipes.etl.library.template.plugin.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration description for a single entity type.
 */
public record ConfigurationDescription(
        /*
         * Resource of description entry.
         */
        Resource resource,
        /*
         * Type of configuration this description is designated for.
         */
        IRI configurationType,
        /*
         * If provided the value is used for all controlProperties,
         * i.e. effectively act as controlProperty but for the whole
         * configuration instance.
         */
        IRI globalControlProperty,
        /*
         * Control information for properties.
         */
        Map<IRI, Member> members
) {

    public record Member(
            IRI controlProperty,
            boolean isPrivate
    ) {

    }

}
