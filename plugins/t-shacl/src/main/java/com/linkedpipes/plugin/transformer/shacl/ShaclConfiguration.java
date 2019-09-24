package com.linkedpipes.plugin.transformer.shacl;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ShaclVocabulary.CONFIG)
public class ShaclConfiguration {

    @RdfToPojo.Property(iri = ShaclVocabulary.HAS_RULE)
    private String rule = "";

    @RdfToPojo.Property(iri = ShaclVocabulary.HAS_FAIL_ON_ERROR)
    private boolean failOnError = true;

    public ShaclConfiguration() {
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

}
