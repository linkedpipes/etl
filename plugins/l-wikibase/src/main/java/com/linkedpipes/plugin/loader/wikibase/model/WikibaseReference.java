package com.linkedpipes.plugin.loader.wikibase.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WikibaseReference {

    private final String iri;

    private final String predicate;

    private List<WikibaseValue> values = new ArrayList<>();

    public WikibaseReference(String iri, String predicate) {
        this.iri = iri;
        this.predicate = predicate;
    }

    public List<WikibaseValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    void addValue(WikibaseValue value) {
        values.add(value);
    }

    public String getPredicate() {
        return predicate;
    }

}
