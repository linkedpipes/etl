package com.linkedpipes.plugin.transformer.filesToStatements;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesToStatementsVocabulary.CONFIG_CLASS)
public class FilesToStatementsConfiguration {

    @RdfToPojo.Property(iri = FilesToStatementsVocabulary.PREDICATE)
    private String predicate;

    public FilesToStatementsConfiguration() {
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

}
