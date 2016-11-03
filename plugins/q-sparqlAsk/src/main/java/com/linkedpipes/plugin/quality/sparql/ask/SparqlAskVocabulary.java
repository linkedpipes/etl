package com.linkedpipes.plugin.quality.sparql.ask;

/**
 *
 */
final class SparqlAskVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/q-sparqlAsk#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_SPARQL = PREFIX + "query";

    public static final String HAS_FAIL_ON_TRUE = PREFIX + "failOnTrue";

    private SparqlAskVocabulary() {
    }

}
