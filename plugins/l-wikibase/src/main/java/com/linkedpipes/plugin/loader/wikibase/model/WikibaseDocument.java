package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikibaseDocument {

    private String iri;

    /**
     * http://www.w3.org/2000/01/rdf-schema#label
     */
    private Map<String, String> labels = new HashMap<>();

    /**
     * https://wikibase.opendata.cz/prop/P3
     */
    private List<WikibaseStatement> statements = new ArrayList<>();

    /**
     * RDF types.
     */
    private List<String> types = new ArrayList<>();

    /**
     * https://wikibase.opendata.cz/prop/novalue/
     */
    private List<String> noValuePredicates = new ArrayList<>();

    public WikibaseDocument(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public String getQid() {
        if (isNew()) {
            return null;
        } else {
            return iri.substring(iri.lastIndexOf("/") + 1);
        }
    }

    public Map<String, String> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    void setLabel(String label, String lang) {
        this.labels.put(lang, label);
    }

    public List<WikibaseStatement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    void addStatement(WikibaseStatement statement) {
        statement.setOwnerQid(getQid());
        statements.add(statement);
    }

    public List<String> getTypes() {
        return types;
    }

    public boolean isNew() {
        return types.contains(
                WikibaseLoaderVocabulary.WIKIDATA_NEW_ENTITY);
    }

    public List<String> getNoValuePredicates() {
        return Collections.unmodifiableList(noValuePredicates);
    }

    void addNoValuePredicate(String predicate) {
        noValuePredicates.add(predicate);
    }

}
