package com.linkedpipes.etl.dataunit.core;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Base configuration entity for core DataUnit instance.
 */
public class DataUnitConfiguration implements Loadable {

    private final String resource;

    private String binding;

    private final List<String> types = new LinkedList<>();

    private String group;

    protected DataUnitConfiguration(String resource) {
        this.resource = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object.asString());
                break;
            case LP_PIPELINE.HAS_BINDING:
                binding = object.asString();
                break;
            case LP_EXEC.HAS_DATA_UNIT_GROUP:
                group = object.asString();
                break;
        }
        return null;
    }

    public String getResource() {
        return resource;
    }

    public String getBinding() {
        return binding;
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public String getGroup() {
        return group;
    }

}
