package com.linkedpipes.plugin.transformer.jsontojsonld;

public final class JsonToJsonLdVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-jsonToJsonLd#";

    public static final String CONFIGURATION = PREFIX + "Configuration";

    public static final String HAS_CONTEXT = PREFIX + "context";

    public static final String HAS_ENCODING = PREFIX + "encoding";

    public static final String HAS_FILE_REFERENCE = PREFIX + "fileReference";

    public static final String HAS_DATA_PREDICATE = PREFIX + "dataPredicate";

    public static final String HAS_TYPE = PREFIX + "type";

    public static final String HAS_FILE_PREDICATE = PREFIX + "filePredicate";

    private JsonToJsonLdVocabulary() {
    }

}
