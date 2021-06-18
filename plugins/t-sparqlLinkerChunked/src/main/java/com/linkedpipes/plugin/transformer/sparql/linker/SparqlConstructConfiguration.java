package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructVocabulary.CONFIG)
public class SparqlConstructConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_QUERY)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_OUTPUT_MODE)
    private String outputMode;

    @RdfToPojo.Property(iri = SparqlConstructVocabulary.HAS_NUMBER_OF_THREADS)
    private int threadCount = 1;

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

    public String getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
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
