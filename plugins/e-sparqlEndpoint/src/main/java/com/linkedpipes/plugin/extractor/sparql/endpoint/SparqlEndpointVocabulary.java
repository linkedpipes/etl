package com.linkedpipes.plugin.extractor.sparql.endpoint;

/**
 *
 * @author Škoda Petr
 */
final class SparqlEndpointVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    private SparqlEndpointVocabulary() {
    }

}
