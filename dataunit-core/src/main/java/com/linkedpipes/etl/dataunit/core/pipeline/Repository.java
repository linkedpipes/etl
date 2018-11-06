package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

class Repository implements Loadable {

    private String resource;

    private List<String> types = new ArrayList<>(2);

    @Override
    public void resource(String resource) {
        this.resource = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case RDF.TYPE:
                types.add(value.asString());
                return null;
            default:
                break;
        }
        return null;
    }

    public String getResource() {
        return resource;
    }

    public List<String> getTypes() {
        return types;
    }

}

