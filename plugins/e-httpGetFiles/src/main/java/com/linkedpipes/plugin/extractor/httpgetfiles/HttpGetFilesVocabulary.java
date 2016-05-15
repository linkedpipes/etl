package com.linkedpipes.plugin.extractor.httpgetfiles;

/**
 *
 * @author Škoda Petr
 */
class HttpGetFilesVocabulary {

    private HttpGetFilesVocabulary() {
    }

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFiles#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String CONFIG_HAS_REFERENCE = PREFIX + "reference";

    public static final String REFERENCE_CLASS = PREFIX + "Reference";

    public static final String REFERENCE_HAS_URI = PREFIX + "fileUri";

    public static final String REFERENCE_HAS_NAME = PREFIX + "fileName";

    public static final String CONFIG_HAS_FOLLOW_REDIRECT
            = PREFIX + "hardRedirect";

}
