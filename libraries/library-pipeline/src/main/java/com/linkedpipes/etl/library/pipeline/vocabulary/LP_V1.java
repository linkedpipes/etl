package com.linkedpipes.etl.library.pipeline.vocabulary;

final public class LP_V1 {

    private LP_V1() {
    }

    public static final String PREF_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String PREFIX_LP = "http://linkedpipes.com/ontology/";

    public static final String PIPELINE = PREFIX_LP + "Pipeline";

    public static final String COMPONENT = PREFIX_LP + "Component";

    public static final String CONNECTION = PREFIX_LP + "Connection";

    public static final String RUN_AFTER = PREFIX_LP + "RunAfter";

    public static final String CONFIGURATION = PREFIX_LP + "Configuration";

    public static final String JAS_TEMPLATE = PREFIX_LP + "JarTemplate";

    public static final String REFERENCE_TEMPLATE = PREFIX_LP + "Template";

    public static final String HAS_PROFILE = PREFIX_LP + "profile";

    public static final String EXECUTION_METADATA =
            PREFIX_LP + "ExecutionMetadata";

    // TODO Move to execution?
    public static final String HAS_EXECUTION_METADATA =
            PREFIX_LP + "executionMetadata";

    public static final String HAS_SAVE_DEBUG_DATA = PREFIX_LP + "saveDebugData";

    public static final String HAS_DELETE_WORKING =
            PREFIX_LP + "deleteWorkingData";

    public static final String RDF_REPOSITORY =
            "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository";

    public static final String HAS_REQ_WORKING =
            "http://linkedpipes.com/resources/requirement/workingDirectory";

    /**
     * Pipeline has execution profile.
     */
    public static final String PROFILE = PREFIX_LP + "ExecutionProfile";

    public static final String HAS_RDF_REPOSITORY_POLICY =
            PREFIX_LP + "rdfRepositoryPolicy";

    public static final String HAS_RDF_REPOSITORY_TYPE =
            PREFIX_LP + "rdfRepositoryType";

    /**
     * Use single RDF repository per execution.
     */
    public static final String SINGLE_REPOSITORY =
            PREFIX_LP + "repository/SingleRepository";

    /**
     * Use RDF repository per input.
     */
    public static final String PER_INPUT_REPOSITORY =
            PREFIX_LP + "repository/PerInputRepository";

    public static final String NATIVE_STORE =
            PREFIX_LP + "repository/NativeStore";

    public static final String MEMORY_STORE =
            PREFIX_LP + "repository/MemoryStore";

    public static final String HAS_LOG_POLICY =
            PREFIX_LP + "logPolicy";

    public static final String LOG_PRESERVE =
            PREFIX_LP + "log/Preserve";

    public static final String LOG_DELETE_ON_SUCCESS =
            PREFIX_LP + "log/DeleteOnSuccess";

    /**
     * TODO Update to ../dataUnit/files/DirectoryMirror.
     */
    public static final String FILE_DATA_UNIT =
            PREFIX_LP + "dataUnit/system/1.0/files/DirectoryMirror";

    /**
     * TODO Update to rdf4j.
     */
    public static final String SINGLE_GRAPH_DATA_UNIT =
            PREFIX_LP + "dataUnit/sesame/1.0/rdf/SingleGraph";

    /**
     * TODO Update to rdf4j.
     */
    public static final String GRAPH_LIST_DATA_UNIT =
            PREFIX_LP + "dataUnit/sesame/1.0/rdf/GraphList";

    public static final String CHUNKED_TRIPLES_DATA_UNIT =
            PREFIX_LP + "dataUnit/sesame/1.0/rdf/Chunked";

    /**
     * Define requirement for working directory.
     */
    public static final String WORKING_DIRECTORY =
            "http://linkedpipes.com/resources/requirement/workingDirectory";

    /**
     * Define requirement for input directory.
     */
    public static final String INPUT_DIRECTORY =
            "http://linkedpipes.com/ontology/requirements/InputDirectory";

    /**
     * Input port.
     */
    public static final String INPUT = "http://linkedpipes.com/ontology/Input";

    /**
     * Output port.
     */
    public static final String OUTPUT =
            "http://linkedpipes.com/ontology/Output";

    /**
     * Pipeline has a component.
     */
    public static final String HAS_COMPONENT = PREFIX_LP + "component";

    /**
     * Pipeline has connections between components.
     */
    public static final String HAS_CONNECTION = PREFIX_LP + "connection";

    /**
     * Port binding.
     */
    public static final String HAS_BINDING = PREFIX_LP + "binding";

    /**
     * Component has a data unit.
     */
    public static final String HAS_DATA_UNIT = PREFIX_LP + "port";

    /**
     * Connection has a source component.
     */
    public static final String HAS_SOURCE_COMPONENT =
            PREFIX_LP + "sourceComponent";

    /**
     * Connection has a source binding.
     */
    public static final String HAS_SOURCE_BINDING = PREFIX_LP + "sourceBinding";

    /**
     * Connection has a target component.
     */
    public static final String HAS_TARGET_COMPONENT =
            PREFIX_LP + "targetComponent";

    /**
     * Connection has a target binding.
     */
    public static final String HAS_TARGET_BINDING = PREFIX_LP + "targetBinding";

    /**
     * Pipeline may have a repository object.
     */
    public static final String HAS_REPOSITORY = PREFIX_LP + "repository";

    public static final String HAS_REQUIREMENT = PREFIX_LP + "requirement";

    /**
     * Path to JAR of given component.
     */
    public static final String HAS_JAR_URL =
            "http://linkedpipes.com/ontology/jar";

    /**
     * Reference to a graph with a configuration, used to reference
     * configuration by frontend. Is not set for reference tempaltes.
     */
    public static final String HAS_CONFIGURATION_GRAPH =
            "http://linkedpipes.com/ontology/configurationGraph";

    /**
     * Component has a template.
     */
    public static final String HAS_TEMPLATE = PREFIX_LP + "template";

    /**
     * Component has types of its configuration class instances in given graph.
     */
    public static final String HAS_CONFIGURATION_ENTITY_DESCRIPTION =
            PREFIX_LP + "configurationDescription";

    public static final String HAS_DISABLED = PREFIX_LP + "disabled";

    /**
     * If true component support configuration control/inheritance.
     */
    public static final String HAS_SUPPORT_CONTROL = PREFIX_LP + "supportControl";

    public static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    public static final String HAS_COLOR =
            "http://linkedpipes.com/ontology/color";

    public static final String JAR_TEMPLATE  =
            "http://linkedpipes.com/ontology/JarTemplate";

    public static final String HAS_COMPONENT_TYPE  =
            "http://linkedpipes.com/ontology/componentType";

    public static final String HAS_KEYWORD  =
            "http://linkedpipes.com/ontology/keyword";

    public static final String HAS_INFO_LINK  =
            "http://linkedpipes.com/ontology/infoLink";

    public static final String HAS_PORT  =
            "http://linkedpipes.com/ontology/port";

    public static final String HAS_DIALOG  =
            "http://linkedpipes.com/ontology/dialog";

    public static final String DIALOG  =
            "http://linkedpipes.com/ontology/Dialog";

    public static final String HAS_NAME  =
            "http://linkedpipes.com/ontology/name";

    public static final String CONFIGURATION_DESCRIPTION  =
            "http://plugins.linkedpipes.com/ontology/ConfigurationDescription";

    public static final String HAS_CONFIG_TYPE =
            "http://plugins.linkedpipes.com/ontology/configuration/type";

    public static final String HAS_DESCRIPTION  =
            "http://purl.org/dc/terms/description";

    public static final String HAS_NOTE  =
            "http://www.w3.org/2004/02/skos/core#note";

    public static final String HAS_KNOWN_AS  =
            "http://www.w3.org/2002/07/owl#sameAs";

    public static final String HAS_PLUGIN_TEMPLATE =
            "http://linkedpipes.com/ontology/pluginTemplate";

    public static final String HAS_X  =
            "http://linkedpipes.com/ontology/x";

    public static final String HAS_Y  =
            "http://linkedpipes.com/ontology/y";

    public static final String HAS_FAILED_EXECUTION_LIMIT  =
            "http://etl.linkedpipes.com/ontology/failedExecutionLimit";

    public static final String HAS_SUCCESSFUL_EXECUTION_LIMIT  =
            "http://etl.linkedpipes.com/ontology/successfulExecutionLimit";

    public static final String HAS_LOG_RETENTION  =
            "http://etl.linkedpipes.com/ontology/logRetentionPolicy";

    public static final String HAS_DATA_RETENTION  =
            "http://etl.linkedpipes.com/ontology/debugDataRetentionPolicy";

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

    public static final String NOTE =
            "http://www.w3.org/2004/02/skos/core#note";

    public static final String HAS_VERSION =
            "http://etl.linkedpipes.com/ontology/version";

}
