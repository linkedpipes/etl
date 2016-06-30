package com.linkedpipes.plugin.extractor.httpget;

/**
 *
 * @author Škoda Petr
 */
final class HttpGetVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_NAME = PREFIX + "fileName";

    public static final String HAS_FOLLOW_REDIRECT = PREFIX + "hardRedirect";

    private HttpGetVocabulary() {
    }

}
