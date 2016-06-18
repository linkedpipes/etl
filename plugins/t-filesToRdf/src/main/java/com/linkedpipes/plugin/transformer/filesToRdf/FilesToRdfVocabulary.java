package com.linkedpipes.plugin.transformer.filesToRdf;

/**
 *
 * @author Škoda Petr
 */
final class FilesToRdfVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-filesToRdf#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_COMMIT_SIZE = PREFIX + "commitSize";

    public static final String HAS_MIME_TYPE = PREFIX + "mimeType";

    private FilesToRdfVocabulary() {
    }

}
