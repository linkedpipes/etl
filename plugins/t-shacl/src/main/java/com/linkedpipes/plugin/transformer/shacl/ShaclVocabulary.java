package com.linkedpipes.plugin.transformer.shacl;

final class ShaclVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-shacl#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_SHAPE = PREFIX + "shapesInTurtle";

    public static final String HAS_FAIL_ON_ERROR = PREFIX + "failOnError";

    private ShaclVocabulary() {
    }

}
