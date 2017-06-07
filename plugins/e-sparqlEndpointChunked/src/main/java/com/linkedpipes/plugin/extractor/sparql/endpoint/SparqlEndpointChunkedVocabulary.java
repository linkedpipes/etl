package com.linkedpipes.plugin.extractor.sparql.endpoint;

final class SparqlEndpointChunkedVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_HEADER_ACCEPT = PREFIX + "headerAccept";

    public static final String HAS_CHUNK_SIZE = PREFIX + "chunkSize";

    public static final String HAS_SKIP_ON_ERROR = PREFIX + "skipOnError";

    private SparqlEndpointChunkedVocabulary() {
    }

}
