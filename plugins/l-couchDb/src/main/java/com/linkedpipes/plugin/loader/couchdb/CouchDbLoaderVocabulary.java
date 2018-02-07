package com.linkedpipes.plugin.loader.couchdb;

public final class CouchDbLoaderVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/l-couchDb#";

    public static final String CONFIGURATION = PREFIX + "Configuration";

    public static final String HAS_URL = PREFIX + "url";

    public static final String HAS_DATABASE = PREFIX + "database";

    public static final String HAS_RECREATE_DATABASE = PREFIX +
            "clearBeforeLoading";

    public static final String HAS_BATCH_SIZE = PREFIX + "batchSize";

    public static final String HAS_USE_AUTHENTICATION =
            PREFIX + "useAuthentication";

    public static final String HAS_USER_NAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

}
