package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP {

    private static final String REPORT_PREFIX =
            "https://vocabulary.etl.linkedpipes.com/report/";

    public static final String REPORT = REPORT_PREFIX + "Report";

    public static final String HAS_TASK = REPORT_PREFIX + "task";

    public static final String HAS_EXCEPTION = REPORT_PREFIX + "exception";

    public static final String EXCEPTION = REPORT_PREFIX + "Exception";

    public static final String HAS_MESSAGE = REPORT_PREFIX + "message";

    public static final String HAS_CLASS = REPORT_PREFIX + "class";

    public static final String HAS_START = REPORT_PREFIX + "start";

    public static final String HAS_END = REPORT_PREFIX + "end";

    public static final String HAS_DURATION = REPORT_PREFIX + "duration";

    public static final String HAS_STATUS = REPORT_PREFIX + "status";

    public static final String SUCCESS = REPORT_PREFIX + "Success";

    public static final String FAILED = REPORT_PREFIX + "Failed";

    private static final String EVENT_PREFIX =
            "http://linkedpipes.com/ontology/events/";

    public static final String HAS_CREATED = EVENT_PREFIX + "created";

    private static final String PROGRESS_PREFIX =
            "http://linkedpipes.com/ontology/progress/";

    public static final String PROGRESS_REPORT =
            PROGRESS_PREFIX + "ProgressReport";

    public static final String HAS_TOTAL = PROGRESS_PREFIX + "total";

    public static final String HAS_CURRENT = PROGRESS_PREFIX + "current";

    private static final String PREFIX = "http://linkedpipes.com/ontology/";

    public static final String HAS_COMPONENT = PREFIX + "component";

    public static final String HAS_WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

}
