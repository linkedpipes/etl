package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = SparqlConstructVocabulary.CONFIG_CLASS)
public class SparqlConstructConfiguration {

    @RdfToPojo.Property(uri = SparqlConstructVocabulary.CONFIG_SPARQL)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    public SparqlConstructConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
