package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = SparqlUpdateVocabulary.CONFIG_CLASS)
public class SparqlUpdateConfiguration {

    @RdfToPojo.Property(uri = SparqlUpdateVocabulary.CONFIG_SPARQL)
    private String query = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    public SparqlUpdateConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
