package com.linkedpipes.plugin.transformer.sparql.linker;

final class SparqlConstructVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-sparqlConstruct#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_OUTPUT_MODE = PREFIX + "outputMode";

    public static final String CREATE_NEW_CHUNK = PREFIX + "createNewChunk";

    public static final String ADD_TO_CHUNK = PREFIX + "addToChunk";

    private SparqlConstructVocabulary() {
    }

}
