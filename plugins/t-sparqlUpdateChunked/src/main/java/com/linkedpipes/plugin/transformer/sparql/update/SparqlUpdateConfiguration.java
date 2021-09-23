package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlUpdateVocabulary.CONFIG_CLASS)
public class SparqlUpdateConfiguration {

    @RdfToPojo.Property(iri = SparqlUpdateVocabulary.CONFIG_SPARQL)
    private String query = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SparqlUpdateVocabulary.HAS_NUMBER_OF_THREADS)
    private int threadCount = 1;

    @RdfToPojo.Property(iri = SparqlUpdateVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    public SparqlUpdateConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
