package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(uri = SparqlEndpointChunkedVocabulary.CONFIG)
public class SparqlEndpointChunkedConfiguration {

    /**
     * Must contains ${VALUES} place holder.
     */
    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_QUERY)
    private String query;

    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_ENDPOINT)
    private String endpoint;

    /**
     * Default graphs can be specified only via the runtime configuration.
     */
    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    /**
     * Used as a Accept value in header.
     */
    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_HEADER_ACCEPT)
    private String transferMimeType;

    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_CHUNK_SIZE)
    private Integer chunkSize;

    public SparqlEndpointChunkedConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getDefaultGraphs() {
        return defaultGraphs;
    }

    public void setDefaultGraphs(List<String> defaultGraphs) {
        this.defaultGraphs = defaultGraphs;
    }

    public String getTransferMimeType() {
        return transferMimeType;
    }

    public void setTransferMimeType(String transferMimeType) {
        this.transferMimeType = transferMimeType;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
}
