package com.linkedpipes.plugin.loader.wikibase.model;

public class Property {

    private final String iri;

    private String type;

    public Property(String iri) {
        this.iri = iri;
    }

    public Property(String iri, String type) {
        this.iri = iri;
        this.type = type;
    }

    public String getIri() {
        return iri;
    }

    public String getType() {
        return type;
    }

    void setType(String property) {
        this.type = property;
    }

}
