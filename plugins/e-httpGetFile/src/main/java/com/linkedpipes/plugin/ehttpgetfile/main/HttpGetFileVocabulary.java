package com.linkedpipes.plugin.ehttpgetfile.main;

public final class HttpGetFileVocabulary {

    public static final String IRI =
            "http://etl.linkedpipes.com/resources/components/e-httpGetFile/" +
                    "0.0.0";

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_NAME = PREFIX + "fileName";

    public static final String HAS_FOLLOW_REDIRECT = PREFIX + "hardRedirect";

    public static final String HAS_UTF8_REDIRECT = PREFIX + "utf8Redirect";

    public static final String HAS_USER_AGENT = PREFIX + "userAgent";

    public static final String ENCODE_URL = PREFIX + "encodeUrl";

    private HttpGetFileVocabulary() {
    }

}
