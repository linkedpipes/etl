package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(uri = SparqlEndpointConstructScrollableCursorVocabulary.CONFIG)
public class SparqlEndpointConstructScrollableCursorConfiguration {

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_PREFIXES)
    private String prefixes;

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_OUTER_CONSTRUCT)
    private String outerConstruct;

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_INNER_SELECT)
    private String innerSelect;

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(
            uri = SparqlEndpointConstructScrollableCursorVocabulary.HAS_PAGE_SIZE)
    private Integer pageSize;

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
}
