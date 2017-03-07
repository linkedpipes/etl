package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlEndpointListVocabulary.CONFIG)
public class SparqlEndpointListConfiguration {

        @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_QUERY)
        private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

        @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_ENDPOINT)
        private String endpoint;

        /**
         * Default graphs can be specified only via the runtime configuration.
         */
        @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_DEFAULT_GRAPH)
        private List<String> defaultGraphs = new ArrayList<>();

        /**
         * Used as a Accept value in header.
         */
        @RdfToPojo.Property(iri = SparqlEndpointListVocabulary.HAS_HEADER_ACCEPT)
        private String transferMimeType = null;

        public SparqlEndpointListConfiguration() {
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
