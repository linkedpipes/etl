package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointListVocabulary.CONFIG)
public class SparqlEndpointListConfiguration {

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_USED_THREADS)
    private int usedThreads = 1;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_TIME_LIMIT)
    private int executionTimeLimit = -1;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_ENCODE_RDF)
    private boolean fixIncomingRdf = false;

    public SparqlEndpointListConfiguration() {
    }

    public int getUsedThreads() {
        return usedThreads;
    }

    public void setUsedThreads(int usedThreads) {
        this.usedThreads = usedThreads;
    }

    public int getExecutionTimeLimit() {
        return executionTimeLimit;
    }

    public void setExecutionTimeLimit(int executionTimeLimit) {
        this.executionTimeLimit = executionTimeLimit;
    }

    public boolean isFixIncomingRdf() {
        return fixIncomingRdf;
    }

    public void setFixIncomingRdf(boolean fixIncomingRdf) {
        this.fixIncomingRdf = fixIncomingRdf;
    }

}
