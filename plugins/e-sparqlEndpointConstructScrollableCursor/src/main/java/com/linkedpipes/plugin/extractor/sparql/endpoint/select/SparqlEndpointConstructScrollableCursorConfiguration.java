package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointConstructScrollableCursorVocabulary.CONFIG)
public class SparqlEndpointConstructScrollableCursorConfiguration {

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_PREFIXES)
    private String prefixes;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_OUTER_CONSTRUCT)
    private String outerConstruct;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_INNER_SELECT)
    private String innerSelect;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_PAGE_SIZE)
    private Integer pageSize;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_ENCODE_RDF)
    private boolean fixIncomingRdf = false;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_AUTH)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_USERNAME)
    private String username;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(
            iri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_USE_TOLERANT_REPOSITORY)
    private boolean useTolerantRepository = false;

    public SparqlEndpointConstructScrollableCursorConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(String prefixes) {
        this.prefixes = prefixes;
    }

    public String getOuterConstruct() {
        return outerConstruct;
    }

    public void setOuterConstruct(String outerConstruct) {
        this.outerConstruct = outerConstruct;
    }

    public String getInnerSelect() {
        return innerSelect;
    }

    public void setInnerSelect(String innerSelect) {
        this.innerSelect = innerSelect;
    }

    public List<String> getDefaultGraphs() {
        return defaultGraphs;
    }

    public void setDefaultGraphs(List<String> defaultGraphs) {
        this.defaultGraphs = defaultGraphs;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFixIncomingRdf() {
        return fixIncomingRdf;
    }

    public void setFixIncomingRdf(boolean fixIncomingRdf) {
        this.fixIncomingRdf = fixIncomingRdf;
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

    public boolean isUseTolerantRepository() {
        return useTolerantRepository;
    }

    public void setUseTolerantRepository(boolean useTolerantRepository) {
        this.useTolerantRepository = useTolerantRepository;
    }

}
