package com.linkedpipes.etl.dataunit.core.pipeline;

import java.util.List;

class Port {

    private final String iri;

    private final List<String> types;

    private final List<String> sources;

    /**
     * The port group is a connected sub-graph. Ports in a group must
     * share the same data repository.
     *
     * Is always greater of equal to zero.
     */
    private Integer group;

    public Port(String iri, List<String> types,List<String> sources) {
        this.iri = iri;
        this.types = types;
        this.sources = sources;
    }

    public String getIri() {
        return iri;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getSources() {
        return sources;
    }

    public Integer getGroup() {
        return group;
    }

    void setGroup(Integer group) {
        this.group = group;
    }
}
