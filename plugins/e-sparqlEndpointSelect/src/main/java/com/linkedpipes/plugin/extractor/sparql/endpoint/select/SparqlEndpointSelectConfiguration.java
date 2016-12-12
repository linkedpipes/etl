package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(uri = SparqlEndpointSelectVocabulary.CONFIG)
public class SparqlEndpointSelectConfiguration {

    @RdfToPojo.Property(uri = SparqlEndpointSelectVocabulary.HAS_QUERY)
    private String query = "SELECT ?type WHERE { ?s a ?type }";

    @RdfToPojo.Property(uri = SparqlEndpointSelectVocabulary.HAS_ENDPOINT)
    private String endpoint;

    /**
     * Default graphs can be specified only via the runtime configuration.
     */
    @RdfToPojo.Property(uri = SparqlEndpointSelectVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    /**
     * Used as a Accept value in header.
     */
    @RdfToPojo.Property(uri = SparqlEndpointSelectVocabulary.HAS_FILE_NAME)
    private String fileName = null;

    public SparqlEndpointSelectConfiguration() {
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
