package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointChunkedListVocabulary.TASK)
public class Task {

    private final String iri;

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

    public Task(String iri) {
        this.iri = iri;
    }

    public Task(Task task, String fileName, String query)
            throws UnsupportedEncodingException {
        this.iri = task.iri + "/" + URLEncoder.encode(fileName, "UTF-8");
        this.query = query;
        this.endpoint = task.endpoint;
        this.defaultGraphs = task.defaultGraphs;
        this.transferMimeType = task.transferMimeType;
        this.fileName = task.fileName;
        this.chunkSize = task.chunkSize;
    }

    public String getIri() {
        return iri;
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

}
