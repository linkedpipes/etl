package com.linkedpipes.plugin.transformer.filesToRdf;

public final class FilesToRdfVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-filesToRdfChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_MIME_TYPE = PREFIX + "mimeType";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    public static final String HAS_SKIP_ON_FAILURE = PREFIX + "softFail";

    public static final String HAS_FILE_REFERENCE = PREFIX + "fileReference";

    public static final String HAS_FILE_PREDICATE = PREFIX + "filePredicate";

    public static final String HAS_NUMBER_OF_THREADS = PREFIX + "threads";

    private FilesToRdfVocabulary() {
    }

}
