package com.linkedpipes.plugin.transformer.filesToRdf;

public final class FilesToRdfVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-filesToRdf#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_MIME_TYPE = PREFIX + "mimeType";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    private FilesToRdfVocabulary() {
    }

}
