package com.linkedpipes.plugin.transformer.sparql.construct;

final class SparqlConstructVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-sparqlConstruct#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_NUMBER_OF_THREADS = PREFIX + "threads";

    public static final String HAS_DEDUPLICATION = PREFIX + "deduplication";

    public static final String HAS_SKIP_ON_FAILURE = PREFIX + "softFail";

    private SparqlConstructVocabulary() {
    }

}
