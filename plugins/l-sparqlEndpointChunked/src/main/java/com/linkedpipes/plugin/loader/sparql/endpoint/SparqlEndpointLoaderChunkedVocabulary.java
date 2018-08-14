package com.linkedpipes.plugin.loader.sparql.endpoint;

final class SparqlEndpointLoaderChunkedVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-sparqlEndpointChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_AUTH = PREFIX + "useAuthentication";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_CLEAR_GRAPH = PREFIX + "clearGraph";

    public static final String HAS_TAGET_GRAPH = PREFIX + "targetGraphURI";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    private SparqlEndpointLoaderChunkedVocabulary() {
    }

}
