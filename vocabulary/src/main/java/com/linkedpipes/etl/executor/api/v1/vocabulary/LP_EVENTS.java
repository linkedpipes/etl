package com.linkedpipes.etl.executor.api.v1.vocabulary;

/**
 * Definition of LinkedPipes specific vocabulary used for execution events.
 */
public final class LP_EVENTS {

    private LP_EVENTS() {

    }

    private static final String PREFIX =
            "http://linkedpipes.com/ontology/";

    public static final String EXECUTION_BEGIN =
            PREFIX + "events/ExecutionBegin";

    public static final String EXECUTION_END =
            PREFIX + "events/ExecutionEnd";

    public static final String EXECUTION_FAILED =
            PREFIX + "events/ExecutionFailed";

    public static final String COMPONENT_BEGIN =
            PREFIX + "events/ComponentBegin";

    public static final String COMPONENT_END =
            PREFIX + "events/ComponentEnd";

    public static final String COMPONENT_FAILED =
            PREFIX + "events/ComponentFailed";

    public static final String HAS_CREATED =
            PREFIX + "events/created";

    public static final String HAS_EXCEPTION =
            PREFIX + "events/exception";

    public static final String HAS_ROOT_EXCEPTION =
            PREFIX + "events/rootException";

    public static final String HAS_REASON =
            PREFIX + "events/reason";

    public static final String PROGRESS_REPORT =
            PREFIX + "progress/ProgressReport";

    public static final String PROGRESS_START =
            PREFIX + "progress/ProgressStart";

    public static final String PROGRESS_DONE = PREFIX + "progress/ProgressDone";

    public static final String HAS_TOTAL = PREFIX + "progress/total";

    public static final String HAS_CURRENT = PREFIX + "progress/current";

}
