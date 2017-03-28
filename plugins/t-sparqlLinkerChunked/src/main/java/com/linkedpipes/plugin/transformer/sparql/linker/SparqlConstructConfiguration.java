package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructVocabulary.CONFIG)
public class SparqlConstructConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_QUERY)
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
