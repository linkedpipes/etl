package com.linkedpipes.plugin.extractor.sparql.endpointlist;

public final class SparqlEndpointListVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointList#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String TASK = PREFIX + "Task";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_HEADER_ACCEPT = PREFIX + "headerAccept";

    public static final String REPORT = PREFIX + "Report";

    public static final String HAS_TASK = PREFIX + "task";

    public static final String HAS_EXCEPTION_MESSAGE =
            PREFIX + "exceptionMessage";

    public static final String HAS_USED_THREADS = PREFIX + "threads";

    private SparqlEndpointListVocabulary() {
    }

}
