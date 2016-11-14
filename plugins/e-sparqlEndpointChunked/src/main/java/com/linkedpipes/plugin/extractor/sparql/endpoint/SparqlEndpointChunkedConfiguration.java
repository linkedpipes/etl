package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(uri = SparqlEndpointChunkedVocabulary.CONFIG)
public class SparqlEndpointChunkedConfiguration {

    @RdfToPojo.Type(uri = SparqlEndpointChunkedVocabulary.QUERY)
    public static class Query {

        @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_QUERY)
        private String query = null;

        /**
         * Default graphs can be specified only via the runtime configuration.
         */
        @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_DEFAULT_GRAPH)
        private List<String> defaultGraphs = new ArrayList<>();

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public List<String> getDefaultGraphs() {
            return defaultGraphs;
        }

        public void setDefaultGraphs(List<String> defaultGraphs) {
            this.defaultGraphs = defaultGraphs;
        }
    }


    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_ENDPOINT)
    private String endpoint;

    /**
     * Used as a Accept value in header.
     */
    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_HEADER_ACCEPT)
    private String transferMimeType = null;

    @RdfToPojo.Property(uri = SparqlEndpointChunkedVocabulary.HAS_QUERY_OBJECT)
    private List<Query> queries = new LinkedList<>();

    public SparqlEndpointChunkedConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public String getTransferMimeType() {
        return transferMimeType;
    }

    public void setTransferMimeType(String transferMimeType) {
        this.transferMimeType = transferMimeType;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(
            List<Query> queries) {
        this.queries = queries;
    }
}
