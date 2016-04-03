package com.linkedpipes.plugin.extractor.httpget;

/**
 *
 * @author Škoda Petr
 */
public class HttpGetVocabulary {

    private static final String PREFIX = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String CONFIG_HAS_URI = PREFIX + "fileUri";

    public static final String CONFIG_HAS_NAME = PREFIX + "fileName";

    public static final String CONFIG_HAS_FOLLOW_REDIRECT
            = PREFIX + "hardRedirect";

}
