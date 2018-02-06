package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointChunkedListVocabulary.CONFIG)
public class SparqlEndpointChunkedListConfiguration implements
        TaskExecutionConfiguration {

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_USED_THREADS)
    private int usedThreads = 1;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_TIME_LIMIT)
    private int executionTimeLimit = -1;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_CHUNK_SIZE)
    private Integer chunkSize;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_ENCODE_RDF)
    private boolean fixIncomingRdf = false;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_USE_TOLERANT_REPOSITORY)
    private boolean useTolerantRepository = false;

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

    public boolean isFixIncomingRdf() {
        return fixIncomingRdf;
    }

    public void setFixIncomingRdf(boolean fixIncomingRdf) {
        this.fixIncomingRdf = fixIncomingRdf;
    }

    @Override
    public int getThreadsNumber() {
        return usedThreads;
    }

    @Override
    public boolean isSkipOnError() {
        return true;
    }

    public boolean isUseTolerantRepository() {
        return useTolerantRepository;
    }

    public void setUseTolerantRepository(boolean useTolerantRepository) {
        this.useTolerantRepository = useTolerantRepository;
    }

}
