package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;

import java.util.ArrayList;
import java.util.List;

class Component implements Loadable {

    private String resource;

    private List<DataUnit> dataUnits = new ArrayList<>();

    @Override
    public void resource(String resource) {
        this.resource = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case "http://linkedpipes.com/ontology/port":
                DataUnit dataUnit = new DataUnit();
                dataUnits.add(dataUnit);
                return dataUnit;
            default:
                break;
        }
        return null;
    }

    public String getResource() {
        return resource;
    }

    public List<DataUnit> getDataUnits() {
        return dataUnits;
    }

}
