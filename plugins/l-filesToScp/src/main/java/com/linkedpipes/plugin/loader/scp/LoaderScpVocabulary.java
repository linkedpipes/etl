package com.linkedpipes.plugin.loader.scp;

/**
 *
 */
public final class LoaderScpVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-filesToScp#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_HOST = PREFIX + "host";

    public static final String HAS_PORT = PREFIX + "port";

    public static final String HAS_TARGET_DIRECTORY
            = PREFIX + "directory";

    public static final String HAS_CREATE_DIRECTORY
            = PREFIX + "createDirectory";

    private LoaderScpVocabulary() {
    }

}
