package com.linkedpipes.plugin.loader.graphstoreprotocol;

/**
 *
 */
public final class GraphStoreProtocolVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-graphStoreProtocol#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_GRAPH = PREFIX + "graph";

    public static final String HAS_TYPE = PREFIX + "repository";

    public static final String HAS_AUTH = PREFIX + "authentification";

    public static final String HAS_USER = PREFIX + "user";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_CHECK_SIZE = PREFIX + "checkSize";

    public static final String HAS_SELECT = PREFIX + "endpointSelect";

    public static final String HAS_CRUD = PREFIX + "endpoint";

    public static final String HAS_REPLACE = PREFIX + "replace";

    private GraphStoreProtocolVocabulary() {
    }

}
