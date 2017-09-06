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

    public static final String HAS_USED_THREADS = PREFIX + "threads";

    public static final String HAS_TIME_LIMIT = PREFIX + "timeLimit";

    public static final String HAS_ENCODE_RDF = PREFIX + "encodeRdf";

    public static final String HAS_GROUP = PREFIX + "group";

    public static final String HAS_TASK_PER_GROUP =
            PREFIX + "taskPerGroupLimit";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    private SparqlEndpointListVocabulary() {
    }

}
