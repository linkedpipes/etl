package com.linkedpipes.plugin.extractor.httpgetfiles;

public final class HttpGetFilesVocabulary {

    private HttpGetFilesVocabulary() {
    }

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-httpGetFiles#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String REFERENCE = PREFIX + "Reference";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_NAME = PREFIX + "name";

    public static final String SKIP_ON_ERROR = PREFIX + "skipOnError";

    public static final String HAS_FOLLOW_REDIRECT
            = PREFIX + "hardRedirect";

    public static final String HAS_UTF8_REDIRECT
            = PREFIX + "utf8Redirect";

    public static final String HAS_HEADER = PREFIX + "header";

    public static final String HAS_THREADS = PREFIX + "threads";

    public static final String HEADER = PREFIX + "Header";

    public static final String HAS_KEY = PREFIX + "key";

    public static final String HAS_VALUE = PREFIX + "value";

    public static final String HAS_DETAIL_LOG = PREFIX + "detailLog";

    public static final String HAS_TIMEOUT = PREFIX + "timeout";

    public static final String HAS_GROUP = PREFIX + "group";

    public static final String HAS_THREADS_PER_GROUP =
            PREFIX + "threadsPerGroup";

    public static final String HAS_HEADER_OBJECT = PREFIX + "hasHeaderObject";

    public static final String HAS_RESPONSE_LINE = PREFIX + "responseLine";

    public static final String HAS_RESPONSE_REPORT = PREFIX + "response";

    public static final String HAS_ERROR_MESSAGE = PREFIX + "errorMessage";

    public static final String HAS_RESPONSE_CODE = PREFIX + "responseCode";

    public static final String ENCODE_URL = PREFIX + "encodeUrl";

}
