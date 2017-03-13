package com.linkedpipes.plugin.extractor.sparql.endpointlist;

final class SparqlEndpointChunkedListVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunkedList#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String TASK = PREFIX + "Task";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_HEADER_ACCEPT = PREFIX + "headerAccept";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String REPORT = PREFIX + "Report";

    public static final String HAS_TASK = PREFIX + "task";

    public static final String HAS_EXCEPTION = PREFIX + "exception";

    public static final String EXCEPTION = PREFIX + "Exception";

    public static final String HAS_EXCEPTION_MESSAGE =
            PREFIX + "message";

    public static final String HAS_EXCEPTION_CLASS =
            PREFIX + "class";

    public static final String HAS_USED_THREADS = PREFIX + "threads";

    public static final String HAS_TIME_LIMIT = PREFIX + "timeLimit";

    public static final String HAS_CHUNK_SIZE = PREFIX + "chunkSize";

    private SparqlEndpointChunkedListVocabulary() {
    }

}
