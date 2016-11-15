package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

final class SparqlEndpointSelectScrollableCursorVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointSelectScrollableCursor#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_OUTER_SELECT = PREFIX + "outerSelect";

    public static final String HAS_PREFIXES = PREFIX + "prefixes";

    public static final String HAS_INNER_SELECT = PREFIX + "innerSelect";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_PAGE_SIZE = PREFIX + "pageSize";

    private SparqlEndpointSelectScrollableCursorVocabulary() {
    }

}
