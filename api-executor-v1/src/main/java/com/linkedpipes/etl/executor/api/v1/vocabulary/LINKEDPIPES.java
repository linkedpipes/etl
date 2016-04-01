package com.linkedpipes.etl.executor.api.v1.vocabulary;

/**
 *
 * @author Petr Škoda
 */
public final class LINKEDPIPES {

    private static final String PREFIX_ONTOLOGY = "http://linkedpipes.com/ontology/";

    private static final String PREFIX_RESOURCE = "http://linkedpipes.com/resources/";

    public static final String PIPELINE = PREFIX_ONTOLOGY + "Pipeline";

    public static final String COMPONENT = PREFIX_ONTOLOGY + "Component";

    public static final String PORT = PREFIX_ONTOLOGY + "Port";

    public static final String HAS_COMPONENT = PREFIX_ONTOLOGY + "component";

    public static final String REPOSITORY = PREFIX_ONTOLOGY + "Repository";

    public static final String HAS_REPOSITORY = PREFIX_ONTOLOGY + "repository";

    public static final String HAS_REQUIREMENT = PREFIX_ONTOLOGY + "requirement";

    public static final String HAS_WORKING_DIRECTORY = PREFIX_ONTOLOGY + "workingDirectory";

    public static final String HAS_EXECUTION_ORDER = PREFIX_ONTOLOGY + "executionOrder";

    public static final String HAS_PORT = PREFIX_ONTOLOGY + "port";

    public static final String HAS_EXECUTION_TYPE = PREFIX_ONTOLOGY + "executionType";

    public static final String HAS_EXECUTION = PREFIX_ONTOLOGY + "execution";

    public static final String HAS_PORT_SOURCE = PREFIX_ONTOLOGY + "source";

    public static final String HAS_BINDING = PREFIX_ONTOLOGY + "binding";

    public static final String HAS_JAR_URI = PREFIX_ONTOLOGY + "jarUri";

    public static final String HAS_CONFIGURATION = PREFIX_ONTOLOGY + "configuration";

    public static final String HAS_LOAD_PATH = PREFIX_ONTOLOGY + "loadPath";

    public static final String HAS_DEBUG = PREFIX_ONTOLOGY + "debug";

    public static final String HAS_DEBUG_PATH = PREFIX_ONTOLOGY + "debugPath";

    public static final String HAS_SOURCE = PREFIX_ONTOLOGY + "dataSource";

    public static class CONFIGURATION {

        private static final String PREFIX_ONTOLOGY = LINKEDPIPES.PREFIX_ONTOLOGY + "configuration/";

        public static final String HAS_ORDER = PREFIX_ONTOLOGY + "order";

        public static final String HAS_RESOURCE = PREFIX_ONTOLOGY + "resource";

        /**
         * If not presented then the graph of this entity is used.
         */
        public static final String HAS_GRAPH = PREFIX_ONTOLOGY + "graph";

    }

    public static class EVENTS {

        private static final String PREFIX_ONTOLOGY = LINKEDPIPES.PREFIX_ONTOLOGY + "events/";

        public static final String EXECUTION_BEGIN = PREFIX_ONTOLOGY + "ExecutionBegin";

        public static final String EXECUTION_END = PREFIX_ONTOLOGY + "ExecutionEnd";

        public static final String EXECUTION_CANCELLED = PREFIX_ONTOLOGY + "ExecutionCancelled";

        public static final String EXECUTION_FAILED = PREFIX_ONTOLOGY + "ExecutionFailed";

        public static final String EXECUTION_STOP = PREFIX_ONTOLOGY + "StopExecution";

        public static final String COMPONENT_BEGIN = PREFIX_ONTOLOGY + "ComponentBegin";

        public static final String COMPONENT_END = PREFIX_ONTOLOGY + "ComponentEnd";

        public static final String COMPONENT_FAILED = PREFIX_ONTOLOGY + "ComponentFailed";

        public static final String INITIALIZATION_FAILED = PREFIX_ONTOLOGY + "InitializationFailed";

        public static final String HAS_CREATED = PREFIX_ONTOLOGY + "created";

        public static final String HAS_EXCEPTION = PREFIX_ONTOLOGY + "exception";

        public static final String HAS_REASON = PREFIX_ONTOLOGY + "reason";

        public static final String HAS_ROOT_EXCEPTION_MESSAGE = PREFIX_ONTOLOGY + "rootException";

        public static class PROGRESS {

            private static final String PREFIX_ONTOLOGY = LINKEDPIPES.PREFIX_ONTOLOGY + "progress/";

            public static final String EVENT_TYPE = PREFIX_ONTOLOGY + "ProgressReport";

            public static final String EVENT_START = PREFIX_ONTOLOGY + "ProgressStart";

            public static final String EVENT_DONE = PREFIX_ONTOLOGY + "ProgressDone";

            public static final String HAS_TOTAL = PREFIX_ONTOLOGY + "total";

            public static final String HAS_CURRENT = PREFIX_ONTOLOGY + "current";

            public static final String CONFIGURATION_CLASS = PREFIX_ONTOLOGY + "Configuration";

            public static final String HAS_STEP_REPORT_SIZE = PREFIX_ONTOLOGY + "reportStepSize";

        }

    }

    public static class REQUIREMENTS {

        private static final String PREFIX_ONTOLOGY = LINKEDPIPES.PREFIX_ONTOLOGY + "requirements/";

        public static final String REQUIREMENT = PREFIX_ONTOLOGY + "Requirement";

        public static final String HAS_SOURCE_PROPERTY = PREFIX_ONTOLOGY + "source";

        public static final String HAS_TARGET_PROPERTY = PREFIX_ONTOLOGY + "target";

        /**
         * Assign working directory under target predicate.
         */
        public static final String TEMP_DIRECTORY = PREFIX_ONTOLOGY + "TempDirectory";

        /**
         * Resolve path to resource from the pipeline definition.
         */
        public static final String RESOLVE_DEFINITION_RESOURCE = PREFIX_ONTOLOGY + "DefinitionResource";

    }

    /**
     * As property is used to tell instance that it should store debug data to given directory.
     */
    public static final String HAS_DEBUG_DIRECTORY = PREFIX_ONTOLOGY + "debugDirectory";

    /**
     * Uri fragments are used to construct URI of objects.
     */
    public static final String HAS_URI_FRAGMENT = PREFIX_ONTOLOGY + "uriFragment";

}
