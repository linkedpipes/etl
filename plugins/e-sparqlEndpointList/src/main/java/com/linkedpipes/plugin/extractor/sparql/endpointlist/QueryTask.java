package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.component.task.GroupTask;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointListVocabulary.TASK)
public class QueryTask implements GroupTask {

    @RdfToPojo.Resource
    public String iri;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_QUERY)
    public String query;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_ENDPOINT)
    public String endpoint;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_DEFAULT_GRAPH)
    public List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_HEADER_ACCEPT)
    public String transferMimeType = null;

    @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_GROUP)
    public Integer group = null;

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

    @Override
    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}
