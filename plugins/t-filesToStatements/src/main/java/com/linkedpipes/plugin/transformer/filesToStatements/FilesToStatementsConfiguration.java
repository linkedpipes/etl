package com.linkedpipes.plugin.transformer.filesToStatements;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = FilesToStatementsVocabulary.CONFIG_CLASS)
public class FilesToStatementsConfiguration {

    @RdfToPojo.Property(uri = FilesToStatementsVocabulary.PREDICATE)
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
