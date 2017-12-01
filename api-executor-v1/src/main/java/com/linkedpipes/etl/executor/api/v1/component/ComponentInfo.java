package com.linkedpipes.etl.executor.api.v1.component;

/**
 * Object with information about component.
 */
class ComponentInfo {

    private final String iri;

    private final String graph;

    public ComponentInfo(String iri, String graph) {
        this.iri = iri;
        this.graph = graph;
    }

    public String getIri() {
        return iri;
    }

    public String getGraph() {
        return graph;
    }

}
