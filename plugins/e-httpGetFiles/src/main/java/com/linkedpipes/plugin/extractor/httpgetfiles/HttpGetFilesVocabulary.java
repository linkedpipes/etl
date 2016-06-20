package com.linkedpipes.plugin.extractor.httpgetfiles;

/**
 *
 * @author Škoda Petr
 */
final class HttpGetFilesVocabulary {

    private HttpGetFilesVocabulary() {
    }

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFiles#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_REFERENCE = PREFIX + "reference";

    public static final String REFERENCE = PREFIX + "Reference";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_NAME = PREFIX + "fileName";

    public static final String SKIP_ON_ERROR = PREFIX + "skipOnError";

    public static final String HAS_FOLLOW_REDIRECT
            = PREFIX + "hardRedirect";

}
