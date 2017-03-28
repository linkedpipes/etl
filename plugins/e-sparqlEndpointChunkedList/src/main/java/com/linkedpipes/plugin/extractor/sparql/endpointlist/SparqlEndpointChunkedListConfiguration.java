package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointChunkedListVocabulary.CONFIG)
public class SparqlEndpointChunkedListConfiguration {

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_USED_THREADS)
    private int usedThreads = 1;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_TIME_LIMIT)
    private int executionTimeLimit = -1;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_CHUNK_SIZE)
    private Integer chunkSize;

    public SparqlEndpointChunkedListConfiguration() {
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

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
}
