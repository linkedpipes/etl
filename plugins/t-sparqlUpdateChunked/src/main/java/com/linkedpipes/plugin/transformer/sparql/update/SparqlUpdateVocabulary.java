package com.linkedpipes.plugin.transformer.sparql.update;

public class SparqlUpdateVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/t-sparqlUpdateChunked#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String CONFIG_SPARQL = PREFIX + "query";

    public static final String HAS_NUMBER_OF_THREADS = PREFIX + "threads";

    public static final String HAS_SKIP_ON_FAILURE = PREFIX + "softFail";

}
