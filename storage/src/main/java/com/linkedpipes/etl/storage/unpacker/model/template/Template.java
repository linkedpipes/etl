package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

public abstract class Template implements Loadable {

    protected String iri;

    protected String configDescriptionGraph;

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                configDescriptionGraph = value.asString();
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public String getConfigDescriptionGraph() {
        return configDescriptionGraph;
    }

    public abstract String getConfigGraph();

}
