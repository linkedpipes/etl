package com.linkedpipes.plugin.loader.wikibase.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WikibaseReference {

    private final String iri;

    private final String predicate;

    private Map<String, List<WikibaseValue>> values = new HashMap<>();

    public WikibaseReference(String iri, String predicate) {
        this.iri = iri;
        this.predicate = predicate;
    }

    public String getPredicate() {
        return predicate;
    }

    void addValue(String property, WikibaseValue value) {
        if (!values.containsKey(property)) {
            values.put(property, new ArrayList<>());
        }
        values.get(property).add(value);
    }

    public Set<String> getValueProperties() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public List<WikibaseValue> getValues(String property) {
        if (values.containsKey(property)) {
            return Collections.unmodifiableList(values.get(property));
        } else {
            return Collections.emptyList();
        }
    }

}
