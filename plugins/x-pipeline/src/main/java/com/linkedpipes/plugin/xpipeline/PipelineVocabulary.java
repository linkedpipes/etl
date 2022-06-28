package com.linkedpipes.plugin.xpipeline;

public final class PipelineVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/x-pipeline#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String INSTANCE = PREFIX + "instance";

    public static final String PIPELINE = PREFIX + "pipeline";

    public static final String SAVE_DEBUG_DATA = PREFIX + "saveDebugData";

    public static final String DELETE_WORKING_DATA =
            PREFIX + "deleteWorkingData";

    public static final String LOG_POLICY = PREFIX + "logPolicy";

    public static final String LOG_LEVEL = PREFIX + "logLevel";

    public static final String LOG_PRESERVE = PREFIX + "PreserveLogs";

    public static final String LOG_DELETE_ON_SUCCESS =
            PREFIX + "DeleteOnSuccess";

}
