package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointSelectVocabulary.CONFIG)
public class SparqlEndpointSelectConfiguration {

    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_QUERY)
    private String query = "SELECT ?type WHERE { ?s a ?type }";

    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_ENDPOINT)
    private String endpoint;

    /**
     * Default graphs can be specified only via the runtime configuration.
     */
    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    /**
     * Used as a Accept value in header.
     */
    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_FILE_NAME)
    private String fileName = null;

    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_AUTH)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_USERNAME)
    private String username;

    @RdfToPojo.Property(iri = SparqlEndpointSelectVocabulary.HAS_PASSWORD)
    private String password;

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

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
