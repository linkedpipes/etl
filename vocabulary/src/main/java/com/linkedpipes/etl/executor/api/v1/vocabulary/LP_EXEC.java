package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP_EXEC {

    private LP_EXEC() {

    }

    private static final String PREFIX = "http://linkedpipes.com/ontology/";

    public static final String TYPE_EXECUTE =
            "http://linkedpipes.com/resources/execution/type/execute";

    public static final String TYPE_MAPPED =
            "http://linkedpipes.com/resources/execution/type/mapped";

    public static final String TYPE_SKIP =
            "http://linkedpipes.com/resources/execution/type/skip";

    /**
     * The working directory is assigned under this predicate.
     */
    public static final String HAS_WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    /**
     * Input directory.
     */
    public static final String HAS_INPUT_DIRECTORY =
            PREFIX + "inputDirectory";

    /**
     * Relative path to the execution root, from which
     * the data should be loaded.
     */
    public static final String HAS_LOAD_PATH = PREFIX + "loadPath";

    /**
     * Reference to an execution.
     */
    public static final String HAS_EXECUTION = PREFIX + "execution";

    /**
     * Dataunit can have a data source, in such case the content
     * is loaded from the source instead of loading from sources.
     */
    public static final String HAS_SOURCE =
            "http://linkedpipes.com/ontology/dataSource";

    /**
     * Component has a reference to the configuration entity.
     */
    public static final String HAS_CONFIGURATION = PREFIX + "configuration";

    /**
     * Represent ordering of elements.
     */
    public static final String HAS_ORDER =
            "http://linkedpipes.com/ontology/configuration/order";

    /**
     * TODO Merge with HAS_ORDER
     */
    public static final String HAS_ORDER_EXEC =
            "http://linkedpipes.com/ontology/executionOrder";

    /**
     * Type of execution that should be used for given component.
     */
    public static final String HAS_EXECUTION_TYPE = PREFIX + "executionType";

    public static final String STATUS_QUEUED =
            "http://etl.linkedpipes.com/resources/status/queued";

    public static final String STATUS_RUNNING =
            "http://etl.linkedpipes.com/resources/status/running";

    public static final String STATUS_FINISHED =
            "http://etl.linkedpipes.com/resources/status/finished";

    public static final String STATUS_CANCELLED =
            "http://etl.linkedpipes.com/resources/status/cancelled";

    public static final String STATUS_CANCELLING =
            "http://etl.linkedpipes.com/resources/status/cancelling";

    public static final String STATUS_FAILED =
            "http://etl.linkedpipes.com/resources/status/failed";

    public static final String STATUS_UNKNOWN =
            "http://etl.linkedpipes.com/resources/status/unknown";


}
