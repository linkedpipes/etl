package com.linkedpipes.plugin.transformer.jsonldtofile;

public final class JsonLdToRdfChunkedVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-jsonLdToRdfChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    public static final String HAS_SKIP_ON_FAILURE = PREFIX + "softFail";

    public static final String HAS_FILE_REFERENCE = PREFIX + "fileReference";

    public static final String HAS_FILE_PREDICATE = PREFIX + "filePredicate";

    private JsonLdToRdfChunkedVocabulary() {
    }

}
