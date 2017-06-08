package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructVocabulary.CONFIG)
public class SparqlConstructConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_QUERY)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_NUMBER_OF_THREADS)
    private int numberOfThreads = 1;

    public SparqlConstructConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

}
