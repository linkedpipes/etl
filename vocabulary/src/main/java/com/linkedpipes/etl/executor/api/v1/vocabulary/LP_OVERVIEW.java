package com.linkedpipes.etl.executor.api.v1.vocabulary;

public class LP_OVERVIEW {

    private static final String PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    public static final String HAS_PIPELINE = PREFIX + "pipeline";

    public static final String HAS_EXECUTION = PREFIX + "execution";

    public static final String HAS_START = PREFIX + "executionStarted";

    public static final String HAS_END = PREFIX + "executionEnded";

    public static final String HAS_STATUS = PREFIX + "executionStatus";

    public static final String HAS_LAST_CHANGE = PREFIX + "lastChange";

    public static final String HAS_PIPELINE_PROGRESS =
            PREFIX + "pipelineProgress";

    public static final String HAS_PROGRESS_TOTAL = PREFIX +
            "numberOfExecutableComponents";

    public static final String HAS_PROGRESS_CURRENT = PREFIX +
            "numberOfExecutedComponents";

}
