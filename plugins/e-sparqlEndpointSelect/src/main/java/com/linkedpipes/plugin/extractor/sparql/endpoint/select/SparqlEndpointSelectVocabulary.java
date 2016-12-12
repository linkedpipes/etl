package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

final class SparqlEndpointSelectVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointSelect#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    private SparqlEndpointSelectVocabulary() {
    }

}
