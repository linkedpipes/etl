(function () {
    "use-strict";

    function getComponent(model, iri) {
        if (model.components[iri] === undefined) {
            model.components[iri] = createComponent(iri);
        }
        return model.components[iri];
    }

    function createComponent(iri) {
        return {
            "iri": iri,
            "messages": [],
            "iris-of-loaded-messages": [],
            "mapping": {
                // True if mapping is on for this component.
                "enabled": true,
                // True if there is possibility to map this component.
                "available": false
            }
        };
    }

    function onExecution(model, resource) {
        model.execution.iri = resource["@id"];
        model.pipeline.iri = jsonld.r.getIRI(resource, LP.HAS_PIPELINE);
        const status = jsonld.r.getIRI(resource, LP.HAS_STATUS);
        model.execution.status.iri = status;
        model.execution.status.running = isExecutionRunning(status);
        model.execution.deleteWorkingData =
            jsonld.r.getBoolean(resource, LP.HAS_DELETE_WORKING_DATA);
    }

    function isExecutionRunning(status) {
        switch (status) {
            case LP.EXEC_FINISHED:
            case LP.EXEC_FAILED:
            case LP.EXEC_CANCELLED:
                return false;
            default:
                return true;
        }

    }

    function onExecutionBegin(model, resource) {
        model.execution.start = getCreated(resource);
    }

    function getCreated(resource) {
        return jsonld.r.getDate(resource, LP.HAS_EVENT_CREATED);
    }

    function onExecutionEnd(model, resource) {
        model.execution.end = getCreated(resource);
    }

    function onComponentBegin(model, resource) {
        const component = getReferencedComponent(model, resource);
        component.start = getCreated(resource);
    }

    function getReferencedComponent(model, resource) {
        const iri = jsonld.r.getIRI(resource, LP.HAS_COMPONENT);
        return getComponent(model, iri);
    }

    function onComponentEnd(model, resource) {
        const component = getReferencedComponent(model, resource);
        component.end = getCreated(resource);
    }

    function onComponentFailed(model, resource) {
        const component = getReferencedComponent(model, resource);
        component.end = getCreated(resource);
        component.failed = {
            "cause": jsonld.r.getPlainString(resource, LP.HAS_EVENT_REASON),
            "rootCause": jsonld.r.getPlainString(resource, LP.HAS_EVENT_ROOT_CAUSE)
        };
    }

    function onComponent(model, resource) {
        const iri = jsonld.r.getId(resource);
        const component = getComponent(model, iri);
        component.status = jsonld.r.getIRI(resource, LP.HAS_STATUS);
        component.order = jsonld.r.getInteger(resource, LP.HAS_EXEC_ORDER);
        component.dataUnits = jsonld.r.getIRIs(resource, LP.HAS_DU);
        component.mapping = convertMapping(component.status);
    }

    function convertMapping(status) {
        switch (status) {
            case LP.EXEC_FINISHED:
            case LP.EXEC_MAPPED:
                return MAPPING_STATUS.FINISHED_MAPPED;
            case LP.EXEC_FAILED:
                return MAPPING_STATUS.FAILED;
            default:
                return MAPPING_STATUS.UNFINISHED;
        }
    }

    function onDataUnit(model, resource) {
        const iri = resource["@id"];
        model.dataUnits[iri] = {
            "iri": iri,
            "binding": jsonld.r.getPlainString(resource, LP.HAS_BINDING),
            "debug": jsonld.r.getPlainString(resource, LP.HAS_DEBUG)
        };
    }

    function onProgressReport(model, resource) {
        const component = getReferencedComponent(model, resource);
        if (component.progress === undefined) {
            component.progress = createProgress(resource);
        }
        // Do not add already added messages.
        const iri = resource["@id"];
        if (component["iris-of-loaded-messages"].indexOf(iri) > -1) {
            return;
        } else {
            component["iris-of-loaded-messages"].push(iri);
        }
        //
        updateComponentProgress(component, resource);
        addMessageToComponent(component, resource);
    }

    function createProgress(resource) {
        return {
            "total": jsonld.r.getInteger(resource, LP.PROGRESS_TOTAL),
            "current": 0,
            "value": 0
        };
    }

    function updateComponentProgress(component, resource) {
        const progress = component.progress;
        const current = jsonld.r.getInteger(resource, LP.PROGRESS_CURRENT);
        // Update component progress (ie. max progress).
        if (current <= progress.current) {
            progress.current = current;
            progress.value = 100 * (progress.current / progress.total);
        }
    }

    function addMessageToComponent(component, resource) {
        component.messages.push({
            "label": jsonld.r.getPlainString(resource, SKOS.PREF_LABEL),
            "created": getCreated(resource, jsonld),
            "order": jsonld.r.getInteger(resource, LP.HAS_ORDER)
        });
    }

    const loadActions = {};

    function loadModelFromJsonLd(model, data, graphIri) {
        console.time("execution-model.loadModel");
        const graph = jsonld.q.getGraph(data, graphIri);
        jsonld.t.iterateResources(graph, (resource) => {
            const types = jsonld.r.getTypes(resource);
            for (let index in types) {
                const type = types[index];
                const action = loadActions[type];
                if (action !== undefined) {
                    action(model, resource);
                }
            }
        });
        console.timeEnd("execution-model.loadModel");
    }

    function initialize() {
        loadActions[LP.EXECUTION] = onExecution;
        loadActions[LP.EXECUTION_BEGIN] = onExecutionBegin;
        loadActions[LP.EXECUTION_END] = onExecutionEnd;
        loadActions[LP.CMP_BEGIN] = onComponentBegin;
        loadActions[LP.CMP_END] = onComponentEnd;
        loadActions[LP.CMP_FAILED] = onComponentFailed;
        loadActions[LP.COMPONENT] = onComponent;
        loadActions[LP.DATA_UNIT] = onDataUnit;
        loadActions[LP.PROGRESS_REPORT] = onProgressReport;
    }

    let LP;
    let SKOS;
    let jsonld;
    let MAPPING_STATUS;

    const module = {
        "loadModelFromJsonLd": loadModelFromJsonLd
    };

    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
            "app/modules/execution-mapping",
        ], (vocabulary, _jsonld, _mapping) => {
            LP = vocabulary.LP;
            SKOS = vocabulary.SKOS;
            jsonld = _jsonld;
            MAPPING_STATUS = _mapping.MAPPING_STATUS;
            initialize();
            return module;
        });
    }

})();
