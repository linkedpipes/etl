package com.linkedpipes.etl.model.vocabulary;

public final class LP {

    public static final String CONFIG_DESCRIPTION =
            "http://plugins.linkedpipes.com/ontology/ConfigurationDescription";

    public static final String CONFIG_SPARQL =
            "http://plugins.linkedpipes.com/ontology/ConfigurationSparql";

    public static final String CONFIG_MAPPING =
            "http://plugins.linkedpipes.com/ontology/ConfigurationMapping";

    public static final String CONFIG_DESC_PROPERTY =
            "http://plugins.linkedpipes.com/ontology/configuration/property";

    public static final String CONFIG_DESC_CONTROL =
            "http://plugins.linkedpipes.com/ontology/configuration/control";

    public static final String IS_PRIVATE =
            "http://plugins.linkedpipes.com/ontology/configuration/private";

    public static final String CONFIG_DESC_MEMBER =
            "http://plugins.linkedpipes.com/ontology/configuration/member";

    public static final String CONFIG_DESC_TYPE =
            "http://plugins.linkedpipes.com/ontology/configuration/type";

    public static final String CONFIG_DESC_MAPPING =
            "http://plugins.linkedpipes.com/ontology/configuration/mapping";

    public static final String CONFIG_DESC_SPARQL =
            "http://plugins.linkedpipes.com/ontology/configuration/sparql";

    public static final String CONFIG_DESC_SOURCE_PROPERTY =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "sourceProperty";

    public static final String CONFIG_DESC_SOURCE_CONTROL =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "sourceControl";

    public static final String CONFIG_DESC_TARGET_PROPERTY =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "targetProperty";

    public static final String CONFIG_DESC_TARGET_CONTROL =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "targetControl";

    public static final String CONFIG_DESC_INSTANCE =
            "http://plugins.linkedpipes.com/ontology/configuration/instance";

    public static final String CONFIG_DESC_PROP_MEMBER =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "PropertyMember";

    public static final String CONFIG_DESC_PROP_ENTITY =
            "http://plugins.linkedpipes.com/ontology/configuration/"
                    + "EntityMember";

    public static final String QUALITY =
            "http://etl.linkedpipes.com/ontology/component/Quality";

    public static final String EXTRACTOR =
            "http://etl.linkedpipes.com/ontology/component/Extractor";

    public static final String TRANSFORMER =
            "http://etl.linkedpipes.com/ontology/component/Transformer";

    public static final String LOADER =
            "http://etl.linkedpipes.com/ontology/component/Loader";

    public static final String EXECUTABLE =
            "http://etl.linkedpipes.com/ontology/component/Executor";

    public static final String HAS_SOURCE_COMPONENT =
            "http://etl.linkedpipes.com/ontology/sourceComponent";

    public static final String HAS_TARGET_COMPONENT =
            "http://etl.linkedpipes.com/ontology/targetComponent";

    public static final String HAS_RESUME =
            "http://etl.linkedpipes.com/ontology/resume";

    public static final String HAS_MAPPING =
            "http://etl.linkedpipes.com/ontology/mapping";

    public static final String HAS_RUN_TO =
            "http://etl.linkedpipes.com/ontology/runTo";

    public static final String EXECUTION_OPTIONS =
            "http://etl.linkedpipes.com/ontology/ExecutionOptions";

    private static final String EXEC_PREFIX =
            "http://etl.linkedpipes.com/ontology/execution/";

    public static final String HAS_EXECUTION_TYPE =
            "http://etl.linkedpipes.com/ontology/execution/type";

    public static final String HAS_METADATA =
            "http://etl.linkedpipes.com/ontology/executionMetadata";

    public static final String HAS_PIPELINE =
            "http://etl.linkedpipes.com/ontology/pipeline";

    public static final String HAS_START =
            "http://etl.linkedpipes.com/ontology/execution/start";

    public static final String HAS_END =
            "http://etl.linkedpipes.com/ontology/execution/end";

    public static final String HAS_STATUS =
            "http://etl.linkedpipes.com/ontology/status";

    public static final String HAS_CREATED =
            "http://etl.linkedpipes.com/ontology/events/created";

    private static final String PROGRESS_PREFIX =
            "http://etl.linkedpipes.com/ontology/progress/";

    public static final String PROGRESS_REPORT =
            PROGRESS_PREFIX + "ProgressReport";

    public static final String HAS_TOTAL = PROGRESS_PREFIX + "total";

    public static final String HAS_CURRENT = PROGRESS_PREFIX + "current";

    private static final String PREFIX = "http://etl.linkedpipes.com/ontology/";

    public static final String HAS_COMPONENT = PREFIX + "component";

    public static final String HAS_WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    public static final String HAS_BUNDLE_NAME = PREFIX + "bundle";

    public static final String EXECUTION_PLAN =
            "http://etl.linkedpipes.com/ontology/execution/ExecutionPlan";

    public static final String SEQUENTIAL_COMPONENT =
            "http://etl.linkedpipes.com/ontology/SequentialComponent";

    public static final String TASK_COMPONENT =
            "http://etl.linkedpipes.com/ontology/TaskComponent";

    public static final String EXECUTION_LIST_METADATA =
            "http://etl.linkedpipes.com/ontology/Metadata";

    public static final String HAS_SERVER_TIME =
            "http://etl.linkedpipes.com/ontology/serverTime";

    public static final String EXECUTION =
            "http://etl.linkedpipes.com/ontology/Execution";

    public static final String HAS_COMPONENT_FINISHED =
            "http://etl.linkedpipes.com/ontology/execution/componentFinished";

    public static final String HAS_COMPONENT_TO_EXECUTE =
            "http://etl.linkedpipes.com/ontology/execution/componentToExecute";

    private static final String EVENT_PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    public static final String EVENT_EXECUTION_BEGIN =
            EVENT_PREFIX + "events/ExecutionBegin";

    public static final String EVENT_EXECUTION_END =
            EVENT_PREFIX + "events/ExecutionEnd";

    public static final String EVENT_EXECUTION_FAILED =
            EVENT_PREFIX + "events/ExecutionFailed";

    public static final String EVENT_COMPONENT_BEGIN =
            EVENT_PREFIX + "events/ComponentBegin";

    public static final String EVENT_COMPONENT_END =
            EVENT_PREFIX + "events/ComponentEnd";

    public static final String EVENT_COMPONENT_FAILED =
            EVENT_PREFIX + "events/ComponentFailed";

    public static final String EVENT_HAS_CREATED =
            EVENT_PREFIX + "events/created";

    public static final String EVENT_HAS_EXCEPTION =
            EVENT_PREFIX + "events/exception";

    public static final String EVENT_HAS_ROOT_EXCEPTION =
            EVENT_PREFIX + "events/rootException";

    public static final String EVENT_HAS_REASON =
            EVENT_PREFIX + "events/reason";

    public static final String EVENT_PROGRESS_REPORT =
            EVENT_PREFIX + "progress/ProgressReport";

    public static final String EVENT_PROGRESS_START =
            EVENT_PREFIX + "progress/ProgressStart";

    public static final String EVENT_PROGRESS_DONE =
            EVENT_PREFIX + "progress/ProgressDone";

    public static final String EVENT_HAS_TOTAL =
            EVENT_PREFIX + "progress/total";

    public static final String EVENT_HAS_CURRENT =
            EVENT_PREFIX + "progress/current";

    public static final String EVENT_HAS_ORDER =
            "http://etl.linkedpipes.com/ontology/order";

    public static final String EVENT_EVENT =
            "http://etl.linkedpipes.com/ontology/Event";

    public static final String EVENT_HAS_EVENT =
            "http://etl.linkdpipes.com/ontology/event";

    public static final String TOMBSTONE =
            "http://etl.linkedpipes.com/ontology/Tombstone";


    public static final String HAS_MONITOR_STATUS =
            "http://etl.linkedpipes.com/ontology/statusMonitor";

    public static final String HAS_FINAL_DATA =
            "http://etl.linkedpipes.com/ontology/finalData";

    public static final String REPORT_PREFIX =
            "https://vocabulary.etl.linkedpipes.com/report/";

    public static final String REPORT_REPORT = REPORT_PREFIX + "Report";

    public static final String REPORT_HAS_TASK = REPORT_PREFIX + "task";

    public static final String REPORT_HAS_EXCEPTION =
            REPORT_PREFIX + "exception";

    public static final String REPORT_EXCEPTION = REPORT_PREFIX + "Exception";

    public static final String REPORT_HAS_MESSAGE = REPORT_PREFIX + "message";

    public static final String REPORT_HAS_CLASS = REPORT_PREFIX + "class";

    public static final String REPORT_HAS_START = REPORT_PREFIX + "start";

    public static final String REPORT_HAS_END = REPORT_PREFIX + "end";

    public static final String REPORT_HAS_DURATION =
            REPORT_PREFIX + "duration";

    public static final String REPORT_HAS_STATUS = REPORT_PREFIX + "status";

    public static final String REPORT_SUCCESS = REPORT_PREFIX + "Success";

    public static final String REPORT_FAILED = REPORT_PREFIX + "Failed";

    private static final String PPL_PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    public static final String UNKNOWN_PIPELINE =
            "http://etl.linkedpipes.com/resource/UnknownPipeline";

    public static final String PIPELINE =
            "http://etl.linkedpipes.com/ontology/Pipeline";

    public static final String HAS_PART =
            "http://etl.linkedpipes.com/ontology/part";

    public static final String COMPONENT =
            "http://etl.linkedpipes.com/ontology/Component";

    public static final String CONNECTION =
            "http://etl.linkedpipes.com/ontology/Connection";

    public static final String RUN_AFTER =
            "http://etl.linkedpipes.com/ontology/RunAfter";

    public static final String CONFIGURATION = PPL_PREFIX + "Configuration";

    public static final String JAR_TEMPLATE =
            "http://etl.linkedpipes.com/ontology/JarTemplate";

    public static final String REFERENCE_TEMPLATE =
            "http://etl.linkedpipes.com/ontology/ReferenceTemplate";

    public static final String HAS_PROFILE = PPL_PREFIX + "profile";

    public static final String HAS_VERSION =
            "http://etl.linkedpipes.com/ontology/version";

    public static final String EXECUTION_METADATA =
            PPL_PREFIX + "ExecutionMetadata";

    public static final String HAS_EXECUTION_METADATA =
            PPL_PREFIX + "executionMetadata";

    public static final String HAS_SAVE_DEBUG_DATA =
            PPL_PREFIX + "saveDebugData";

    public static final String HAS_DELETE_WORKING =
            PPL_PREFIX + "deleteWorkingData";

    public static final String RDF_REPOSITORY =
            "http://etl.linkedpipes.com/"
                    + "ontology/dataUnit/sesame/1.0/Repository";

    public static final String HAS_REQ_WORKING =
            "http://etl.linkedpipes.com/"
                    + "resources/requirement/workingDirectory";

    /**
     * Pipeline has execution profile.
     */
    public static final String PROFILE = PPL_PREFIX + "ExecutionProfile";

    public static final String HAS_RDF_REPOSITORY_POLICY =
            PPL_PREFIX + "rdfRepositoryPolicy";

    public static final String HAS_RDF_REPOSITORY_TYPE =
            PPL_PREFIX + "rdfRepositoryType";

    /**
     * Use single RDF repository per execution.
     */
    public static final String SINGLE_REPOSITORY =
            PPL_PREFIX + "repository/SingleRepository";

    /**
     * Use RDF repository per input.
     */
    public static final String PER_INPUT_REPOSITORY =
            PPL_PREFIX + "repository/PerInputRepository";

    public static final String NATIVE_STORE =
            PPL_PREFIX + "repository/NativeStore";

    public static final String MEMORY_STORE =
            PPL_PREFIX + "repository/MemoryStore";

    public static final String HAS_LOG_POLICY =
            PPL_PREFIX + "logPolicy";

    public static final String EXEC_HAS_LOG_POLICY =
            EXEC_PREFIX + "logPolicy";

    public static final String LOG_PRESERVE =
            PPL_PREFIX + "log/Preserve";

    public static final String LOG_DELETE_ON_SUCCESS =
            PPL_PREFIX + "log/DeleteOnSuccess";

    public static final String FILE_DATA_UNIT =
            "http://etl.linkedpipes.com/ontology/port/files/FilesPort";

    public static final String SINGLE_GRAPH_DATA_UNIT_MEMORY =
            PPL_PREFIX + "dataUnit/rdf4j/MemorySingleGraph";

    public static final String SINGLE_GRAPH_DATA_UNIT_NATIVE =
            PPL_PREFIX + "dataUnit/rdf4j/NativeSingleGraph";

    public static final String GRAPH_LIST_DATA_UNIT_MEMORY =
            PPL_PREFIX + "dataUnit/rdf4j/MemoryGraphList";

    public static final String GRAPH_LIST_DATA_UNIT_NATIVE =
            PPL_PREFIX + "dataUnit/rdf4j/NativeGraphList";

    public static final String CHUNKED_TRIPLES_DATA_UNIT =
            PPL_PREFIX + "dataUnit/sesame/1.0/rdf/Chunked";

    public static final String FILES_PORT =
            "http://etl.linkedpipes.com/ontology/port/files/FilesPort";

    public static final String RDF4J_QUADS_PORT =
            "http://etl.linkedpipes.com/ontology/port/rd4j/QuadsPort";

    public static final String RDF4J_TRIPLES_PORT =
            "http://etl.linkedpipes.com/ontology/port/rd4j/TriplesPort";


    public static final String REPOSITORY_RDF4J_TRIPLES_PORT =
            "http://etl.linkedpipes.com/ontology/port/rd4j/"
                    + "RepositoryTriplesPort";

    /**
     * Define requirement for working directory.
     */
    public static final String WORKING_DIRECTORY =
            "http://etl.linkedpipes.com/resources/requirement/"
                    + "workingDirectory";

    /**
     * Define requirement for input directory.
     */
    public static final String INPUT_DIRECTORY =
            "http://etl.linkedpipes.com/ontology/requirements/InputDirectory";

    /**
     * Input port.
     */
    public static final String INPUT =
            "http://etl.linkedpipes.com/ontology/Input";

    /**
     * Output port.
     */
    public static final String OUTPUT =
            "http://etl.linkedpipes.com/ontology/Output";

    /**
     * Pipeline has connections between components.
     */
    public static final String HAS_CONNECTION = PPL_PREFIX + "connection";

    /**
     * Port binding.
     */
    public static final String HAS_BINDING = PPL_PREFIX + "binding";

    /**
     * Component has a data unit.
     */
    public static final String HAS_DATA_UNIT = PPL_PREFIX + "port";

    /**
     * Connection has a source binding.
     */
    public static final String HAS_SOURCE_BINDING =
            PPL_PREFIX + "sourceBinding";


    public static final String EXEC_HAS_TARGET_COMPONENT =
            EXEC_PREFIX + "targetComponent";

    /**
     * Connection has a target binding.
     */
    public static final String HAS_TARGET_BINDING =
            PPL_PREFIX + "targetBinding";

    /**
     * Pipeline may have a repository object.
     */
    public static final String HAS_REPOSITORY = PPL_PREFIX + "repository";

    public static final String HAS_REQUIREMENT = PPL_PREFIX + "requirement";

    /**
     * Path to JAR of given component.
     */
    public static final String HAS_JAR_URL =
            "http://etl.linkedpipes.com/ontology/jar";

    /**
     * Reference to a graph with a configuration, used to reference
     * configuration by frontend. Is not set for reference templates.
     */
    public static final String HAS_CONFIGURATION_GRAPH =
            "http://etl.linkedpipes.com/ontology/configurationGraph";

    public static final String HAS_CONFIGURATION_DESC_GRAPH =
            "http://etl.linkedpipes.com/ontology/"
                    + "configurationDescriptionGraph";

    /**
     * Component has a template.
     */
    public static final String HAS_TEMPLATE = PPL_PREFIX + "template";

    /**
     * Component has types of it's configuration class instances.
     */
    public static final String HAS_CONFIGURATION_ENTITY_DESCRIPTION =
            PPL_PREFIX + "configurationDescription";

    public static final String HAS_DISABLED =
            "http://etl.linkedpipes.com/ontology/disabled";

    public static final String HAS_X =
            "http://etl.linkedpipes.com/ontology/x";

    public static final String HAS_Y =
            "http://etl.linkedpipes.com/ontology/y";

    public static final String HAS_BUNDLE = PPL_PREFIX + "bundle";

    public static final String HAS_CONTAINS = PPL_PREFIX + "contains";

    public static final String HAS_NAME = PPL_PREFIX + "name";

    public static final String HAS_CLASS_NAME = PPL_PREFIX + "className";

    /**
     * If true component support configuration control/inheritance.
     */
    public static final String HAS_SUPPORT_CONTROL =
            PPL_PREFIX + "supportControl";

    public static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    private static final String ETL_PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    public static final String OVERVIEW = ETL_PREFIX + "ExecutionOverview";

    public static final String HAS_EXECUTION = ETL_PREFIX + "execution";

    public static final String EXECUTION_PROFILE =
            "http://etl.linkedpipes.com/ontology/ExecutionProfile";

    /**
     * Reference to an execution.
     */
    public static final String EXEC_HAS_EXECUTION =
            EXEC_PREFIX + "execution";


    public static final String HAS_LAST_CHANGE = ETL_PREFIX + "lastChange";

    public static final String HAS_PIPELINE_PROGRESS =
            ETL_PREFIX + "pipelineProgress";

    public static final String HAS_PROGRESS_TOTAL =
            ETL_PREFIX + "execution/componentToExecute";

    public static final String HAS_PROGRESS_CURRENT =
            ETL_PREFIX + "execution/componentFinished";

    public static final String HAS_PROGRESS_TOTAL_MAP =
            ETL_PREFIX + "execution/componentToMap";

    public static final String HAS_PROGRESS_MAPPED =
            ETL_PREFIX + "execution/componentMapped";

    public static final String HAS_PROGRESS_EXECUTED =
            ETL_PREFIX + "execution/componentExecuted";

    public static final String HAS_DIRECTORY_SIZE =
            ETL_PREFIX + "directorySize";

    public static final String HAS_ACTION = ETL_PREFIX + "action";

    public static final String EXEC_HAS_ACTION =
            EXEC_PREFIX + "action";

    public static final String HAS_PID = ETL_PREFIX + "pid";

    public static final String HAS_PATH = ETL_PREFIX + "path";


    private static final String PLUGIN_PREFIX =
            "http://plugins.linkedpipes.com/ontology/configuration/";

    private static final String PLUGIN_RESOURCE =
            "http://plugins.linkedpipes.com/resource/configuration/";

    /**
     * Object control, cause given value to inherit from the
     * previous object.
     */
    public static final String INHERIT = PLUGIN_RESOURCE + "Inherit";

    /**
     * Object control, force value of current object to all
     * follow objects.
     */
    public static final String FORCE = PLUGIN_RESOURCE + "Force";

    /**
     * Object control, inherit value from ancestor and force it to
     * all successors.
     */
    public static final String INHERIT_AND_FORCE =
            PLUGIN_RESOURCE + "InheritAndForce";

    /**
     * Object control, replace value from parent if that value is not forced.
     */
    public static final String NONE = PLUGIN_RESOURCE + "None";

    /**
     * Object control info. This value is not used by control, but rather used
     * by the object merger to indicate that certain value was forced
     * in the instance.
     */
    public static final String FORCED = PLUGIN_RESOURCE + "Forced";

    /**
     * Type of description object.
     */
    public static final String DESCRIPTION =
            "http://plugins.linkedpipes.com/ontology/ConfigurationDescription";

    /**
     * Point to type the object describe.
     */
    public static final String HAS_DESCRIBE =
            "http://plugins.linkedpipes.com/ontology/configuration/type";

    /**
     * Description has member entities.
     */
    public static final String HAS_MEMBER = PLUGIN_PREFIX + "member";

    /**
     * Type of member entity description.
     */
    public static final String MEMBER = PLUGIN_PREFIX + "ConfigurationMember";

    /**
     * Member entity refer to value property.
     */
    public static final String HAS_PROPERTY = PLUGIN_PREFIX + "property";

    /**
     * Member entity refer to control value.
     */
    public static final String HAS_CONTROL = PLUGIN_PREFIX + "control";

    /**
     * If true given description member represent a complex type.
     * The complex objects are merged on the object level.
     */
    public static final String IS_COMPLEX = PLUGIN_PREFIX + "complex";


    public static final String TYPE_EXECUTE =
            "http://etl.linkedpipes.com/resources/execution/type/execute";

    public static final String TYPE_MAPPED =
            "http://etl.linkedpipes.com/resources/execution/type/mapped";

    public static final String TYPE_SKIP =
            "http://etl.linkedpipes.com/resources/execution/type/skip";

    public static final String EXECUTION_FULL =
            "http://etl.linkedpipes.com/resources/executionType/Full";

    public static final String EXECUTION_DEBUG_TO =
            "http://etl.linkedpipes.com/resources/executionType/DebugTo";

    public static final String EXECUTION_DEBUG_FROM =
            "http://etl.linkedpipes.com/resources/executionType/DebugFrom";

    public static final String EXECUTION_DEBUG_FROM_TO =
            "http://etl.linkedpipes.com/resources/executionType/DebugFromTo";

    public static final String HAS_SIZE =
            "http://etl.linkedpipes.com/ontology/execution/size";

    public static final String HAS_TYPE =
            "http://etl.linkedpipes.com/ontology/execution/type";


    public static final String HAS_PIPELINE_EXECUTION_TYPE =
            EXEC_PREFIX + "type";

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
     * Dataunit/component can have a data source.
     */
    public static final String HAS_SOURCE =
            EXEC_PREFIX + "source";

    public static final String HAS_TARGET =
            EXEC_PREFIX + "target";

    /**
     * Delete working data after pipeline execution.
     */
    public static final String HAS_DELETE_WORKING_DATA =
            PREFIX + "deleteWorkingData";

    public static final String STATUS_MAPPED =
            "http://etl.linkedpipes.com/resources/status/mapped";

    public static final String STATUS_QUEUED =
            "http://etl.linkedpipes.com/resources/status/queued";

    public static final String STATUS_DELETED =
            "http://etl.linkedpipes.com/resources/status/deleted";

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

    public static final String STATUS_DANGLING =
            "http://etl.linkedpipes.com/resources/status/dangling";

    public static final String STATUS_UNRESPONSIVE =
            "http://etl.linkedpipes.com/resources/status/unresponsive";

    public static final String PORT_SOURCE = PREFIX + "PortSource";

    public static final String HAS_DATA_SOURCE = PREFIX + "dataSource";

    public static final String DATA_UNIT =
            "http://etl.linkedpipes.com/ontology/DataUnit";

    /**
     * Define groups of data units of same type that must share the same
     * repository.
     */
    public static final String HAS_DATA_UNIT_GROUP = PREFIX + "dataUnitGroup";

    public static final String HAS_OPTIONS =
            EXEC_PREFIX + "options";

    public static final String LOG_DELETE_ALL =
            EXEC_PREFIX + "log/DeleteAll";

    public static final String LOG_PRESERVE_ALL =
            EXEC_PREFIX + "log/PreserveAll";

    public static final String LOG_PRESERVE_WARN =
            EXEC_PREFIX + "log/PreserveWarn";

    public static final String CLOSE_DATA_UNIT =
            EXEC_PREFIX + "CloseDataUnit";

    public static final String CREATE_COMPONENT =
            EXEC_PREFIX + "CreateComponent";

    public static final String CREATE_DATA_UNIT =
            EXEC_PREFIX + "CreateDataUnit";

    public static final String CREATE_DATA_UNIT_MANAGER =
            EXEC_PREFIX + "CreateDataUnitManager";

    public static final String EXECUTE_SEQUENTIAL =
            EXEC_PREFIX + "ExecuteSequentialComponent";

    public static final String EXECUTE_TASK =
            EXEC_PREFIX + "ExecuteTaskComponent";

    public static final String INITIALIZE_COMPONENT =
            EXEC_PREFIX + "InitializeComponent";

    public static final String LOAD_DATA_UNIT_FROM_DIRECTORY =
            EXEC_PREFIX + "LoadDataUnit";

    public static final String MAP_COMPONENT =
            EXEC_PREFIX + "MapComponent";

    public static final String MERGE_DATA_UNIT =
            EXEC_PREFIX + "MergeDataUnit";

    public static final String MAP_DATA_UNIT =
            EXEC_PREFIX + "MapDataUnit";

    public static final String RESUME_DATA_UNIT =
            EXEC_PREFIX + "ResumeDataUnit";

    public static final String RESUME_TASK =
            EXEC_PREFIX + "ResumeTaskComponent";

    public static final String SAVE_DATA_UNIT_DEBUG =
            EXEC_PREFIX + "SaveDataUnitDebugData";

    public static final String HAS_GROUP =
            EXEC_PREFIX + "group";

    public static final String HAS_INPUT =
            EXEC_PREFIX + "input";

    public static final String HAS_DIRECTORY =
            EXEC_PREFIX + "directory";

    public static final String CLOSE_DATA_UNIT_MANGER =
            EXEC_PREFIX + "CloseDataUnitManager";

    public static final String HAS_MANAGER =
            EXEC_PREFIX + "manager";

    public static final String HAS_ORDER =
            EXEC_PREFIX + "order";

    public static final String HAS_AFTER_SUCCESS =
            EXEC_PREFIX + "afterSuccess";

    public static final String HAS_DATA_UNIT_TYPE =
            EXEC_PREFIX + "type";

    public static final String HAS_COMPONENT_START =
            EXEC_PREFIX + "componentStart";

    public static final String HAS_COMPONENT_MAPPED =
            EXEC_PREFIX + "sourceComponent";

    public static final String HAS_CONFIGURATION_DESCRIPTION_GRAPH =
            EXEC_PREFIX + "configurationDescriptionGraph";

    public static final String HAS_SOURCE_EXECUTION =
            EXEC_PREFIX + "sourceExecution";

    public static final String HAS_SOURCE_DIRECTORY =
            EXEC_PREFIX + "sourceDirectory";

    public static final String HAS_PORT =
            "http://etl.linkedpipes.com/ontology/port";

}
