package com.linkedpipes.plugin.transformer.jsonldtofile;

public final class JsonLdToRdfVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-jsonLdToRdf#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    public static final String HAS_SKIP_ON_FAILURE = PREFIX + "softFail";

    private JsonLdToRdfVocabulary() {
    }

}
