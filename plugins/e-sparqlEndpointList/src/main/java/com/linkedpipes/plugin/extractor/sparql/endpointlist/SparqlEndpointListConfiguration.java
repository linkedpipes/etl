package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointListVocabulary.CONFIG)
public class SparqlEndpointListConfiguration {

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_USED_THREADS)
    private int usedThreads = 1;

    public SparqlEndpointListConfiguration() {
    }

    public int getUsedThreads() {
        return usedThreads;
    }

    public void setUsedThreads(int usedThreads) {
        this.usedThreads = usedThreads;
    }

}
