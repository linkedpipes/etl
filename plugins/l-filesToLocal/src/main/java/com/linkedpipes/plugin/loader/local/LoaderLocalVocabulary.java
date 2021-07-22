package com.linkedpipes.plugin.loader.local;

public final class LoaderLocalVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-filesToLocal#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_PATH = PREFIX + "path";

    public static final String HAS_FILE_PERMISSIONS =
            PREFIX + "filePermissions";

    public static final String HAS_DIRECTORY_PERMISSIONS =
            PREFIX + "directoryPermissions";

    private LoaderLocalVocabulary() {
    }

}
