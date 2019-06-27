package com.linkedpipes.plugin.loader.wikibase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class WikibaseDocument {

    public static class Statement {

        private String predicate;

        private String value;

        public Statement(String predicate, String value) {
            this.predicate = predicate;
            this.value = value;
        }

        public String getPredicate() {
            return predicate;
        }

        public String getValue() {
            return value;
        }

    }

    private String qid;

    private Map<String, String> labels = new HashMap<>();

    private List<Statement> statements = new ArrayList<>();

    private List<String> types = new ArrayList<>();

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
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

    public void addStatement(String predicate, String value) {
        statements.add(new Statement(predicate, value));
    }

    public List<String> getTypes() {
        return types;
    }

}
