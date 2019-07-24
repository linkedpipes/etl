package com.linkedpipes.plugin.loader.wikibase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class WikibaseDocument {

    // <https://wikibase.opendata.cz/entity/statement/Q2066-99C627E9-A027-4C4D-85E6-DA0AD38DEC45>
    public static class Statement {

        private final String iri;

        private final String predicateIri;

        // <https://wikibase.opendata.cz/prop/statement/P3>
        private String value;

        //
        private final List<String> types = new ArrayList<>();

        private String ownerQid;

        public Statement(String iri, String predicate) {
            this.iri = iri;
            this.predicateIri = predicate;
        }

        public String getIri() {
            return iri;
        }

        public String getPredicate() {
            return predicateIri.substring(predicateIri.lastIndexOf("/") + 1);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public List<String> getTypes() {
            return types;
        }

        public boolean isNew() {
            return types.contains(
                    WikibaseLoaderVocabulary.WIKIDATA_NEW_ENTITY);
        }

        public boolean isForDelete() {
            return types.contains(
                    WikibaseLoaderVocabulary.WIKIDATA_DELETE_ENTITY);
        }

        public void setOwnerQid(String ownerQid) {
            this.ownerQid = ownerQid;
        }

        public String getStatementId() {
            if (isNew()) {
                return null;
            } else {
                return ownerQid + "$" + getQid();
            }
        }

        private String getQid() {
            String id = iri.substring(iri.lastIndexOf("/"));
            return id.substring(id.indexOf("-") + 1);
        }

    }

    private String iri;

    // http://www.w3.org/2000/01/rdf-schema#label
    private Map<String, String> labels = new HashMap<>();

    // ...
    // https://wikibase.opendata.cz/prop/P3
    private List<Statement> statements = new ArrayList<>();

    //
    private List<String> types = new ArrayList<>();

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
        return labels;
    }

    public void setLabel(String label, String lang) {
        this.labels.put(lang, label);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void addStatement(Statement statement) {
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

}
