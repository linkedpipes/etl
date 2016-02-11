package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = SparqlEndpointVocabulary.CONFIG_CLASS)
public class SparqlEndpointConfiguration {

    @RdfToPojo.Property(uri = SparqlEndpointVocabulary.HAS_QUERY)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(uri = SparqlEndpointVocabulary.HAS_ENDPOINT)
    private String endpoint;

    public SparqlEndpointConfiguration() {
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

}
