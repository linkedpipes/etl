package com.linkedpipes.plugin.extractor.httpget;

public final class HttpGetVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_NAME = PREFIX + "fileName";

    public static final String HAS_FOLLOW_REDIRECT = PREFIX + "hardRedirect";

    public static final String HAS_USER_AGENT = PREFIX + "userAgent";

    public static final String ENCODE_URL = PREFIX + "encodeUrl";

    private HttpGetVocabulary() {
    }

}
