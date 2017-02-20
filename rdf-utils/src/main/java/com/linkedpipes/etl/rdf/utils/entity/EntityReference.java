package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfSource;

/**
 * Describe reference to the RDF entity.
 */
public class EntityReference {

    private final String resource;

    private final String graph;

    private final RdfSource source;

    public EntityReference(String resource, String graph,
            RdfSource source) {
        this.resource = resource;
        this.graph = graph;
        this.source = source;
    }

    public String getResource() {
        return resource;
    }

    public String getGraph() {
        return graph;
    }

    public RdfSource getSource() {
        return source;
    }

}
