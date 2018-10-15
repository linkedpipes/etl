package com.linkedpipes.plugin.transformer.property.linker;

final class PropertyLinkerVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-propertyLinkerChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_CHUNK_PREDICATE = PREFIX + "chunkPredicate";

    public static final String HAS_DATA_PREDICATE = PREFIX + "referencePredicate";

    private PropertyLinkerVocabulary() {
    }

}
