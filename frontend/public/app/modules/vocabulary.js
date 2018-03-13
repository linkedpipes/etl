((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

    // TODO Split into LP and SKOS vocabulary, so it can be used right after import.

    const DATA_UNIT = "http://linkedpipes.com/ontology/dataUnit/";

    const LP = {
        "DU_SESAME_CHUNKED": DATA_UNIT + "sesame/1.0/rdf/Chunked",
        "DU_SESAME_SINGLE_GRAPH": DATA_UNIT + "sesame/1.0/rdf/SingleGraph",
        "HAS_PIPELINE": "http://etl.linkedpipes.com/ontology/pipeline",
        "HAS_STATUS": "http://etl.linkedpipes.com/ontology/status",
        "HAS_DELETE_WORKING_DATA": "http://linkedpipes.com/ontology/deleteWorkingData",
        "EXEC_FINISHED": "http://etl.linkedpipes.com/resources/status/finished",
        "EXEC_FAILED" : "http://etl.linkedpipes.com/resources/status/failed",
        "EXEC_CANCELLED": "http://etl.linkedpipes.com/resources/status/cancelled",
        "EXEC_MAPPED": "http://etl.linkedpipes.com/resources/status/mapped",
        "HAS_EVENT_CREATED": "http://linkedpipes.com/ontology/events/created",
        "HAS_COMPONENT" : "http://linkedpipes.com/ontology/component",
        "HAS_EVENT_REASON" : "http://linkedpipes.com/ontology/events/reason",
        "HAS_EVENT_ROOT_CAUSE": "http://linkedpipes.com/ontology/events/rootException",
        "HAS_EXEC_ORDER": "http://linkedpipes.com/ontology/executionOrder",
        "HAS_DU" : "http://etl.linkedpipes.com/ontology/dataUnit",
        "HAS_BINDING": "http://etl.linkedpipes.com/ontology/binding",
        "HAS_DEBUG" : "http://etl.linkedpipes.com/ontology/debug",
        "PROGRESS_TOTAL" : "http://linkedpipes.com/ontology/progress/total",
        "PROGRESS_CURRENT" : "http://linkedpipes.com/ontology/progress/current",
        "HAS_ORDER": "http://linkedpipes.com/ontology/order",
        "EXECUTION": "http://etl.linkedpipes.com/ontology/Execution",
        "EXECUTION_BEGIN": "http://linkedpipes.com/ontology/events/ExecutionBegin",
        "EXECUTION_END": "http://linkedpipes.com/ontology/events/ExecutionEnd",
        "CMP_BEGIN": "http://linkedpipes.com/ontology/events/ComponentBegin",
        "CMP_END" : "http://linkedpipes.com/ontology/events/ComponentEnd",
        "CMP_FAILED" : "http://linkedpipes.com/ontology/events/ComponentFailed",
        "COMPONENT": "http://linkedpipes.com/ontology/Component",
        "DATA_UNIT": "http://etl.linkedpipes.com/ontology/DataUnit",
        "PROGRESS_REPORT": "http://linkedpipes.com/ontology/progress/ProgressReport",
        "EXEC_OPTIONS" : "http://etl.linkedpipes.com/ontology/ExecutionOptions",
        "SAVE_DEBUG" : "http://linkedpipes.com/ontology/saveDebugData",
        "DELETE_WORKING": "http://linkedpipes.com/ontology/deleteWorkingData",
        "HAS_TAG": "http://etl.linkedpipes.com/ontology/tag",
        "PIPELINE": "http://linkedpipes.com/ontology/Pipeline",
        "TOMBSTONE" : "http://linkedpipes.com/ontology/Tombstone",
        "HAS_REPO_POLICY": "rdfRepositoryPolicy",
        "HAS_REPO_TYPE": "rdfRepositoryType"
    };

    const SKOS = {
        "PREF_LABEL": "http://www.w3.org/2004/02/skos/core#prefLabel"
    };

    return {
        "LP" : LP,
        "SKOS": SKOS
    };

});
