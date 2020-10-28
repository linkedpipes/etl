package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlUpdateVocabulary.CONFIG_CLASS)
public class SparqlUpdateConfiguration {

    @RdfToPojo.Property(iri = SparqlUpdateVocabulary.CONFIG_SPARQL)
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
