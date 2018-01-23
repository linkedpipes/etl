package com.linkedpipes.etl.executor.rdf.entity;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;

/**
 * Object used to reference RDF entity.
 */
public class EntityReference {

    private final String resource;

    private final String graph;

    private final BackendRdfSource source;

    public EntityReference(String resource, String graph,
            BackendRdfSource source) {
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

    public BackendRdfSource getSource() {
        return source;
    }

}
