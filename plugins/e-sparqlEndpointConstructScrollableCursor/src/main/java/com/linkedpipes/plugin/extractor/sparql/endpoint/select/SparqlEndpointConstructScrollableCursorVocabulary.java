package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

final class SparqlEndpointConstructScrollableCursorVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointConstructScrollableCursor#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_OUTER_CONSTRUCT = PREFIX + "outerConstruct";

    public static final String HAS_PREFIXES = PREFIX + "prefixes";

    public static final String HAS_INNER_SELECT = PREFIX + "innerSelect";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_PAGE_SIZE = PREFIX + "pageSize";

    public static final String HAS_ENCODE_RDF = PREFIX + "encodeRdf";

    private SparqlEndpointConstructScrollableCursorVocabulary() {
    }

}
