package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP_EXEC {

    private static final String PREFIX = "http://linkedpipes.com/ontology/";

    public static final String TYPE_EXECUTE =
            "http://linkedpipes.com/resources/execution/type/execute";

    public static final String TYPE_MAPPED =
            "http://linkedpipes.com/resources/execution/type/mapped";

    public static final String TYPE_SKIP =
            "http://linkedpipes.com/resources/execution/type/skip";

    public static final String EXECUTION_FULL =
            "http://linkedpipes.com/resources/executionType/Full";

    public static final String EXECUTION_DEBUG_TO =
            "http://linkedpipes.com/resources/executionType/DebugTo";

    public static final String EXECUTION_DEBUG_FROM =
            "http://linkedpipes.com/resources/executionType/DebugFrom";

    public static final String EXECUTION_DEBUG_FROM_TO =
            "http://linkedpipes.com/resources/executionType/DebugFromTo";

    public static final String EXECUTION =
            "http://etl.linkedpipes.com/ontology/Execution";

    public static final String HAS_SIZE =
            "http://etl.linkedpipes.com/ontology/execution/size";

    /**
     * The working directory is assigned under this predicate.
     */
    public static final String HAS_WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    public static final String HAS_TARGET_COMPONENT =
            "http://linkedpipes.com/ontology/execution/targetComponent";

    public static final String HAS_PIPELINE_EXECUTION_TYPE =
            "http://linkedpipes.com/ontology/execution/type";

    public static final String HAS_METADATA = PREFIX + "executionMetadata";

    public static final String HAS_EXECUTION_PROFILE = PREFIX + "profile";

    /**
     * Input directory.
     */
    public static final String HAS_INPUT_DIRECTORY =
            PREFIX + "inputDirectory";

    public static final String HAS_DEBUG_PATH = PREFIX + "debugPath";

    /**
     * Relative path to the execution root, from which
     * the data should be loaded.
     */
    public static final String HAS_LOAD_PATH = PREFIX + "loadPath";

    /**
     * Reference to an execution.
     */
    public static final String HAS_EXECUTION = PREFIX + "execution";

    public static final String HAS_EXECUTION_ETL =
            "http://etl.linkedpipes.com/ontology/execution";

    public static final String HAS_DEBUG =
            "http://etl.linkedpipes.com/ontology/debug";

    /**
     * Relative path to the data used by port.
     */
    public static final String HAS_DATA_PATH =
            "http://etl.linkedpipes.com/ontology/dataPath";

    /**
     * Dataunit can have a data source, in such case the content
     * is loaded from the source instead of loading from sources.
     */
    public static final String HAS_SOURCE =
            "http://linkedpipes.com/ontology/dataSource";

    /**
     * If true save debug data.
     */
    public static final String HAS_SAVE_DEBUG_DATA =
            "http://linkedpipes.com/ontology/saveDebugData";

    /**
     * Delete working data after pipeline execution.
     */
    public static final String HAS_DELETE_WORKING_DATA =
            PREFIX + "deleteWorkingData";

    /**
     * Represent ordering of elements.
     */
    public static final String HAS_ORDER =
            "http://linkedpipes.com/ontology/configuration/order";

    /**
     * TODO Merge with HAS_ORDER.
     */
    public static final String HAS_ORDER_EXEC =
            "http://linkedpipes.com/ontology/executionOrder";

    /**
     * Type of execution that should be used for given component.
     */
    public static final String HAS_EXECUTION_TYPE = PREFIX + "executionType";

    public static final String STATUS_MAPPED =
            "http://etl.linkedpipes.com/resources/status/mapped";

    public static final String STATUS_QUEUED =
            "http://etl.linkedpipes.com/resources/status/queued";

    public static final String STATUS_INITIALIZING =
            "http://etl.linkedpipes.com/resources/status/initializing";

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

    public static final String PORT_SOURCE = PREFIX + "PortSource";

    public static final String HAS_DATA_SOURCE = PREFIX + "dataSource";

    public static final String HAS_DATA_UNIT =
            "http://etl.linkedpipes.com/ontology/dataUnit";

    public static final String DATA_UNIT =
            "http://etl.linkedpipes.com/ontology/DataUnit";

    /**
     * Define groups of data units of same type that must share the same
     * repository.
     */
    public static final String HAS_DATA_UNIT_GROUP = PREFIX + "dataUnitGroup";

}
