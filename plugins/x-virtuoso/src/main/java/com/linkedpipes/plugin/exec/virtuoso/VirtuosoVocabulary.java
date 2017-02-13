package com.linkedpipes.plugin.exec.virtuoso;

public final class VirtuosoVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/x-virtuoso#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String VIRTUOSO_URI = PREFIX + "uri";

    public static final String USERNAME = PREFIX + "username";

    public static final String PASSWORD = PREFIX + "password";

    public static final String CLEAR_GRAPH = PREFIX + "clearGraph";

    public static final String LOAD_DIRECTORY_PATH = PREFIX + "directory";

    public static final String LOAD_FILE_NAME = PREFIX + "fileName";

    public static final String TARGET_GRAPH = PREFIX + "graph";

    public static final String STATUS_UPDATE_INTERVAL =
            PREFIX + "updateInterval";

    public static final String CLEAR_LOAD_GRAPH = PREFIX + "clearSqlLoadTable";

}
