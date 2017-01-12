package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP_EXEC {

    private LP_EXEC() {

    }

    private static final String PREFIX = "http://linkedpipes.com/ontology/";

    /**
     * TODO Change to resource
     */
    public static final String TYPE_EXECUTE = PREFIX + "Execute";

    /**
     * TODO Change to resource
     */
    public static final String TYPE_MAPPED = PREFIX + "Mapped";

    /**
     * TODO Change to resource
     */
    public static final String TYPE_SKIP = PREFIX + "Skip";

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
    public static final String HAS_SOURCE = PREFIX + "source";

    /**
     * Component has a reference to the configuration entity.
     */
    public static final String HAS_CONFIGURATION = PREFIX + "configuration";

    /**
     * Represent ordering of elements.
     */
    public static final String HAS_ORDER = PREFIX + "order";

    /**
     * Type of execution that should be used for given component.
     */
    public static final String HAS_EXECUTION_TYPE = PREFIX + "executionType";

}
