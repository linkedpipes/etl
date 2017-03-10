package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointListVocabulary.TASK)
public class Task {

    private final String iri;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_QUERY)
    private String query;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_HEADER_ACCEPT)
    private String transferMimeType = null;

    public Task(String iri) {
        this.iri = iri;
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

}
