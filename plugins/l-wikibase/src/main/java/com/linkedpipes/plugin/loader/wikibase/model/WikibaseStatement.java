package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WikibaseStatement {

    private final String iri;

    private final String predicate;

    /**
     * A simple value if presented
     */
    private RdfValue value;

    private final List<String> types = new ArrayList<>();

    private String ownerQid;

    private Map<String, List<WikibaseValue>> qualifierValues = new HashMap<>();

    private List<WikibaseValue> statementValues = new LinkedList<>();

    private List<WikibaseReference> references = new ArrayList<>();

    private boolean isSomeValue = false;

    WikibaseStatement(String iri, String predicate) {
        this.iri = iri;
        this.predicate = predicate;
    }

    static WikibaseStatement someValue(String predicate) {
        WikibaseStatement result = new WikibaseStatement(null, predicate);
        result.isSomeValue = true;
        return result;
    }

    public String getIri() {
        return iri;
    }

    public String getPredicate() {
        return predicate;
    }

    public RdfValue getSimpleValue() {
        return value;
    }

    void setValue(RdfValue value) {
        this.value = value;
    }

    public List<String> getTypes() {
        return types;
    }

    public boolean isNew() {
        return types.contains(WikibaseLoaderVocabulary.WIKIDATA_NEW_ENTITY)
                || iri == null;
    }

    public boolean isForDelete() {
        return types.contains(
                WikibaseLoaderVocabulary.WIKIDATA_DELETE_ENTITY);
    }

    void setOwnerQid(String ownerQid) {
        this.ownerQid = ownerQid;
    }

    public String getStatementId() {
        if (isNew()) {
            return null;
        } else {
            String id = iri.substring(iri.lastIndexOf("/"));
            return ownerQid + "$" + id.substring(id.indexOf("-") + 1);
        }
    }

    void addQualifierValue(String property, WikibaseValue value) {
        if (!qualifierValues.containsKey(property)) {
            qualifierValues.put(property, new ArrayList<>());
        }
        qualifierValues.get(property).add(value);
    }

    public Set<String> getQualifierProperties() {
        return Collections.unmodifiableSet(qualifierValues.keySet());
    }

    public List<WikibaseValue> getQualifierValues(String property) {
        if (qualifierValues.containsKey(property)) {
            return Collections.unmodifiableList(qualifierValues.get(property));
        } else {
            return Collections.emptyList();
        }
    }

    public List<WikibaseValue> getStatementValues() {
        return Collections.unmodifiableList(statementValues);
    }

    void addStatementValue(WikibaseValue value) {
        statementValues.add(value);
    }

    public List<WikibaseReference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    void addReference(WikibaseReference value) {
        references.add(value);
    }

    public boolean isSomeValue() {
        return isSomeValue;
    }

}
