package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointSelectScrollableCursorVocabulary.CONFIG)
public class SparqlEndpointSelectScrollableCursorConfiguration {

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_PREFIXES)
    private String prefixes;

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_OUTER_SELECT)
    private String outerSelect;

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_INNER_SELECT)
    private String innerSelect;

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(
            iri = SparqlEndpointSelectScrollableCursorVocabulary.HAS_PAGE_SIZE)
    private Integer pageSize;

    public SparqlEndpointSelectScrollableCursorConfiguration() {
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

    public String getOuterSelect() {
        return outerSelect;
    }

    public void setOuterSelect(String outerSelect) {
        this.outerSelect = outerSelect;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
