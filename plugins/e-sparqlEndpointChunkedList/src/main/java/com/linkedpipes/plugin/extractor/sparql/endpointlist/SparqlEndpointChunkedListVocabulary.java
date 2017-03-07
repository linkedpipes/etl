package com.linkedpipes.plugin.extractor.sparql.endpointlist;

final class SparqlEndpointChunkedListVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunkedList#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_HEADER_ACCEPT = PREFIX + "headerAccept";

    public static final String HAS_CHUNK_SIZE = PREFIX + "chunkSize";

    private SparqlEndpointChunkedListVocabulary() {
    }

}
