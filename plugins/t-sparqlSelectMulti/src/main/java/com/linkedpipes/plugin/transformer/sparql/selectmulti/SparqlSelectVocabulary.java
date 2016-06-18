package com.linkedpipes.plugin.transformer.sparql.selectmulti;

/**
 *
 * @author Škoda Petr
 */
final class SparqlSelectVocabulary {

    private static final String PREFIX = "http://plugins.linkedpipes.com/ontology/t-sparqlSelect#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    private SparqlSelectVocabulary() {
    }
}
