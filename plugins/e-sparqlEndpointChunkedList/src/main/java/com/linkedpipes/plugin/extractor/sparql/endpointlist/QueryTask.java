package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.component.task.GroupTask;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointChunkedListVocabulary.TASK)
public class QueryTask implements GroupTask {

    @RdfToPojo.Resource
    public String iri;

    @RdfToPojo.Property(iri = SparqlEndpointChunkedListVocabulary.HAS_QUERY)
    private String query;

    @RdfToPojo.Property(iri = SparqlEndpointChunkedListVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_HEADER_ACCEPT)
    private String transferMimeType = null;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_FILE_NAME)
    private String fileName = null;

    @RdfToPojo.Property(
            iri = SparqlEndpointChunkedListVocabulary.HAS_CHUNK_SIZE)
    private int chunkSize;

    @Override
    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public Object getGroup() {
        return this.endpoint;
    }

}
