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
        model.pipeline.iri = jsonldModule.r.getIRI(resource, LP.HAS_PIPELINE);
        const status = jsonldModule.r.getIRI(resource, LP.HAS_STATUS);
        model.execution.status.iri = status;
        model.execution.status.running = isExecutionRunning(status);
        model.execution.deleteWorkingData =
            jsonldModule.r.getBoolean(resource, LP.HAS_DELETE_WORKING_DATA);
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
        return jsonldModule.r.getDate(resource, LP.HAS_EVENT_CREATED);
    }

    function onExecutionEnd(model, resource) {
        model.execution.end = getCreated(resource);
    }

    function onComponentBegin(model, resource) {
        const component = getReferencedComponent(model, resource);
        component.start = getCreated(resource);
    }

    function getReferencedComponent(model, resource) {
        const iri = jsonldModule.r.getIRI(resource, LP.HAS_COMPONENT);
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
            "cause": jsonldModule.r.getPlainString(resource, LP.HAS_EVENT_REASON),
            "rootCause": jsonldModule.r.getPlainString(resource, LP.HAS_EVENT_ROOT_CAUSE)
        };
    }

    function onComponent(model, resource) {
        const iri = jsonldModule.r.getId(resource);
        const component = getComponent(model, iri);
        component.status = jsonldModule.r.getIRI(resource, LP.HAS_STATUS);
        component.order = jsonldModule.r.getInteger(resource, LP.HAS_EXEC_ORDER);
        component.dataUnits = jsonldModule.r.getIRIs(resource, LP.HAS_DU);
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
            "binding": jsonldModule.r.getPlainString(resource, LP.HAS_BINDING),
            "debug": jsonldModule.r.getPlainString(resource, LP.HAS_DEBUG)
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
            "total": jsonldModule.r.getInteger(resource, LP.PROGRESS_TOTAL),
            "current": 0,
            "value": 0
        };
    }

    function updateComponentProgress(component, resource) {
        const progress = component.progress;
        const current = jsonldModule.r.getInteger(resource, LP.PROGRESS_CURRENT);
        // Update component progress (ie. max progress).
        if (current <= progress.current) {
            progress.current = current;
            progress.value = 100 * (progress.current / progress.total);
        }
    }

    function addMessageToComponent(component, resource) {
        component.messages.push({
            "label": jsonldModule.r.getPlainString(resource, SKOS.PREF_LABEL),
            "created": getCreated(resource, jsonldModule),
            "order": jsonldModule.r.getInteger(resource, LP.HAS_ORDER)
        });
    }

    const loadActions = {};

    function loadModelFromJsonLd(model, data, jsonld_service) {
        console.time("execution-model.loadModel");
        jsonld_service.iterateObjects(data, (resource, graph) => {
            const types = jsonldModule.r.getTypes(resource);
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
    let jsonldModule;
    let MAPPING_STATUS;

    const module = {
        "loadModelFromJsonLd": loadModelFromJsonLd
    };

    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
            "app/modules/mapping",
        ], (vocabulary, _jsonld, _mapping) => {
            LP = vocabulary.LP;
            SKOS = vocabulary.SKOS;
            jsonldModule = _jsonld;
            MAPPING_STATUS = _mapping.MAPPING_STATUS;
            initialize();
            return module;
        });
    }

})();
