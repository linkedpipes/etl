package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructVocabulary.CONFIG)
public class SparqlConstructConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_QUERY)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_NUMBER_OF_THREADS)
    private int numberOfThreads = 1;

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_DEDUPLICATION)
    private boolean useDeduplication = false;

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

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

    public boolean isUseDeduplication() {
        return useDeduplication;
    }

    public void setUseDeduplication(boolean useDeduplication) {
        this.useDeduplication = useDeduplication;
    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
