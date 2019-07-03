((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

    // TODO Split into LP and SKOS vocabulary, so it can be used right after import.

    const ETL_LP = "http://etl.linkedpipes.com/ontology/";
    const LP = "http://linkedpipes.com/ontology/";
    const ELT_LP_RES = "http://etl.linkedpipes.com/resources/";

    const LP_VOCAB = {
        "DU_SESAME_CHUNKED": LP + "dataUnit/sesame/1.0/rdf/Chunked",
        "DU_SESAME_SINGLE_GRAPH": LP + "dataUnit/sesame/1.0/rdf/SingleGraph",
        "HAS_PIPELINE": ETL_LP + "pipeline",
        "HAS_EXECUTION": LP + "execution",
        "HAS_EXECUTION_ETL": ETL_LP + "execution",
        "HAS_STATUS": ETL_LP + "status",
        "HAS_DELETE_WORKING_DATA": LP + "deleteWorkingData",
        "EXEC_FINISHED": "http://etl.linkedpipes.com/resources/status/finished",
        "EXEC_INITIALIZING": "http://etl.linkedpipes.com/resources/status/initializing",
        "EXEC_FAILED" : "http://etl.linkedpipes.com/resources/status/failed",
        "EXEC_CANCELLED": "http://etl.linkedpipes.com/resources/status/cancelled",
        "EXEC_CANCELLING": "http://etl.linkedpipes.com/resources/status/cancelling",
        "EXEC_MAPPED": "http://etl.linkedpipes.com/resources/status/mapped",
        "EXEC_INVALID": "http://etl.linkedpipes.com/resources/status/invalid",
        "EXEC_DANGLING": "http://etl.linkedpipes.com/resources/status/dangling",
        "EXEC_QUEUED": "http://etl.linkedpipes.com/resources/status/queued",
        "EXEC_RUNNING": "http://etl.linkedpipes.com/resources/status/running",
        "EXEC_UNRESPONSIVE": "http://etl.linkedpipes.com/resources/status/unresponsive",
        "HAS_EVENT_CREATED": LP + "events/created",
        "HAS_COMPONENT" : LP + "component",
        "HAS_EVENT_REASON" : LP + "events/reason",
        "HAS_EVENT_ROOT_CAUSE": LP + "events/rootException",
        "HAS_EXEC_ORDER": LP + "order",
        "HAS_DU" : ETL_LP + "dataUnit",
        "HAS_PORT_BINDING": LP + "binding",
        "HAS_DU_BINDING": ETL_LP + "binding",
        "HAS_DEBUG" : ETL_LP + "debug",
        "PROGRESS_TOTAL" : LP + "progress/total",
        "PROGRESS_CURRENT" : LP + "progress/current",
        "HAS_ORDER": LP + "order",
        "EXECUTION": ETL_LP + "Execution",
        "EXECUTION_BEGIN": LP + "events/ExecutionBegin",
        "EXECUTION_END": LP + "events/ExecutionEnd",
        "CMP_BEGIN": LP + "events/ComponentBegin",
        "CMP_END" : LP + "events/ComponentEnd",
        "CMP_FAILED" : LP + "events/ComponentFailed",
        "COMPONENT": LP + "Component",
        "TEMPLATE" : LP + "Template",
        "HAS_TEMPLATE" : LP + "template",
        "DATA_UNIT": ETL_LP + "DataUnit",
        "PROGRESS_REPORT": LP + "progress/ProgressReport",
        "EXEC_OPTIONS" : ETL_LP + "ExecutionOptions",
        "SAVE_DEBUG" : LP + "saveDebugData",
        "DELETE_WORKING": LP + "deleteWorkingData",
        "HAS_TAG": ETL_LP + "tag",
        "PIPELINE": LP + "Pipeline",
        "TOMBSTONE" : LP + "Tombstone",
        "HAS_REPO_POLICY": LP + "rdfRepositoryPolicy",
        "HAS_REPO_TYPE": LP + "rdfRepositoryType",
        "UPDATE_OPTIONS" : LP + "UpdateOptions",
        "HAS_IMPORT_TEMPLATES": LP +  "importTemplates",
        "HAS_UPDATE_TEMPLATES": LP + "updateTemplates",
        "JAR_TEMPLATE": LP +  "JarTemplate",
        "REF_TEMPLATE": LP +  "Template",
        "HAS_KEYWORD": LP + "keyword",
        "HAS_COLOR": LP + "color",
        "HAS_CONFIGURATION": LP + "configurationGraph",
        "HAS_TYPE": LP + "componentType",
        "HAS_DIALOG": LP + "dialog",
        "HAS_NAME": LP + "name",
        "HAS_PORT": LP + "port",
        "HAS_USED_IN": LP + "usedIn",
        "HAS_INFO_LINK": LP + "infoLink",
        "HAS_SUPPORT_CONTROL": LP + "supportControl",
        "TASK_LIST": LP + "TaskList",
        "RUNTIME_CONFIGURATION": LP + "RuntimeConfiguration",
        "PORT": LP + "Port",
        "OUTPUT": LP + "Output",
        "INPUT": LP + "Input",
        "COMPONENT_QUEUED": ELT_LP_RES + "status/queued",
        "COMPONENT_INITIALIZING": ELT_LP_RES + "status/initializing",
        "COMPONENT_RUNNING": ELT_LP_RES + "status/running",
        "COMPONENT_FINISHED": ELT_LP_RES + "status/finished",
        "COMPONENT_MAPPED": ELT_LP_RES + "status/mapped",
        "COMPONENT_FAILED": ELT_LP_RES + "status/failed",
        "HAS_X": "http://linkedpipes.com/ontology/x",
        "HAS_Y": "http://linkedpipes.com/ontology/y",
        "HAS_DISABLED":  LP + "disabled",
        "HAS_SOURCE_COMPONENT": LP + "sourceComponent",
        "HAS_SOURCE_BINDING": LP + "sourceBinding",
        "HAS_TARGET_COMPONENT": LP + "targetComponent",
        "HAS_TARGET_BINDING": LP + "targetBinding",
        "CONNECTION": "http://linkedpipes.com/ontology/Connection",
        "RUN_AFTER": "http://linkedpipes.com/ontology/RunAfter",
        "VERTEX":  LP + "Vertex",
        "HAS_VERTEX": "http://linkedpipes.com/ontology/vertex",
        "HAS_EXECUTION_PROFILE": "http://linkedpipes.com/ontology/profile",
        "EXECUTION_PROFILE": "http://linkedpipes.com/ontology/ExecutionProfile",
        "RDF_SINGLE_REPOSITORY": LP + "repository/SingleRepository",
        "LOADER": "http://etl.linkedpipes.com/ontology/component/type/Loader",
        "EXECUTOR": "http://etl.linkedpipes.com/ontology/component/type/Executor",
        "RUN_TO": "http://etl.linkedpipes.com/ontology/runTo",
        "EXECUTION_MAPPING": "http://etl.linkedpipes.com/ontology/executionMapping",
        "MAPPING": "http://etl.linkedpipes.com/ontology/mapping",
        "MAPPING_SOURCE": "http://etl.linkedpipes.com/ontology/mappingSource",
        "MAPPING_TARGET": "http://etl.linkedpipes.com/ontology/mappingTarget",
        "INHERIT": "http://plugins.linkedpipes.com/resource/configuration/Inherit"
    };

    const SKOS_VOCAB = {
        "PREF_LABEL": "http://www.w3.org/2004/02/skos/core#prefLabel"
    };

    const DCTERMS_VOCAB = {
        "DESCRIPTION": "http://purl.org/dc/terms/description"
    };

    return {
        "LP" : LP_VOCAB,
        "SKOS": SKOS_VOCAB,
        "DCTERMS": DCTERMS_VOCAB
    };

});
