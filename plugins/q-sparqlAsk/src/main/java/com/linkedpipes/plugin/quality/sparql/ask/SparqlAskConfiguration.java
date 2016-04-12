package com.linkedpipes.plugin.quality.sparql.ask;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = SparqlAskVocabulary.CONFIG_CLASS)
public class SparqlAskConfiguration {

    @RdfToPojo.Property(uri = SparqlAskVocabulary.HAS_SPARQL)
    private String query = "ASK { ?s ?p ?o }";

    /**
     * If true execution fail if ASK return true, else fail if ASK
     * return false.
     */
    @RdfToPojo.Property(uri = SparqlAskVocabulary.HAS_FAIL_ON_TRUE)
    private boolean failOnTrue;

    public SparqlAskConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isFailOnTrue() {
        return failOnTrue;
    }

    public void setFailOnTrue(boolean failOnTrue) {
        this.failOnTrue = failOnTrue;
    }

}
