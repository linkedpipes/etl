((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
        ], definition);
    }
})((_vocabulary, jsonld) => {
    "use strict";

    const LP = _vocabulary.LP;
    const SKOS = _vocabulary.SKOS;

    function onComponentBegin(model, resource) {
        const component = getReferencedComponent(model, resource);
        component.start = getCreated(resource);
    }

    function getReferencedComponent(model, resource) {
        const iri = jsonld.r.getIRI(resource, LP.HAS_COMPONENT);
        return getComponent(model, iri);
    }

    function getComponent(model, iri) {
        if (model[iri] === undefined) {
            model[iri] = createComponent(iri);
        }
        return model[iri];
    }

    function createComponent(iri) {
        return {
            "iri": iri,
            "iris-of-loaded-messages": [],
            "messages": []
        };
    }

    function getCreated(resource) {
        return jsonld.r.getDate(resource, LP.HAS_EVENT_CREATED);
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

    function loadFromJsonLd(model, data, graphIri) {
        console.time("message-loader");
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
        console.timeEnd("message-loader");
    }

    //
    //
    //

    const loadActions = {};

    (function initialize() {
        loadActions[LP.CMP_BEGIN] = onComponentBegin;
        loadActions[LP.CMP_END] = onComponentEnd;
        loadActions[LP.CMP_FAILED] = onComponentFailed;
        loadActions[LP.PROGRESS_REPORT] = onProgressReport;
    })();

    return {
        "loadFromJsonLd": loadFromJsonLd
    };

});
