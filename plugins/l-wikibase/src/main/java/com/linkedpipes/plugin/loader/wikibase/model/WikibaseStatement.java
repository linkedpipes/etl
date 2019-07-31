package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WikibaseStatement {

    private final String iri;

    private final String predicate;

    /**
     * A simple value if presented
     */
    private String value;

    private final List<String> types = new ArrayList<>();

    private String ownerQid;

    private List<WikibaseValue> qualifierValues = new ArrayList<>();

    private List<WikibaseValue> statementValues = new ArrayList<>();

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

    public String getSimpleValue() {
        return value;
    }

    void setValue(String value) {
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

    void addQualifierValue(WikibaseValue value) {
        qualifierValues.add(value);
    }

    public List<WikibaseValue> getQualifierValues() {
        return Collections.unmodifiableList(qualifierValues);
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
