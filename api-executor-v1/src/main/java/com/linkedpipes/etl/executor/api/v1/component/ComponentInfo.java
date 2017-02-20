package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

/**
 * Object with information about component.
 */
class ComponentInfo implements RdfLoader.Loadable<String> {

    private final String iri;

    private final String graph;

    public ComponentInfo(String iri, String graph) {
        this.iri = iri;
        this.graph = graph;
    }

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        return null;
    }

    public String getIri() {
        return iri;
    }

    public String getGraph() {
        return graph;
    }

}
