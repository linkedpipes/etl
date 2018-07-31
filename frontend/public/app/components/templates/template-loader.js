((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["vocabulary", "jsonld"], definition);
    }
})((vocabulary, jsonld) => {

    const LP = vocabulary.LP;
    const SKOS = vocabulary.SKOS;
    const DCTERMS = vocabulary.DCTERMS;

    function parseResponse(data, response) {
        // Each component is stored in a graph.
        jsonld.q.iterateGraphs(response.data, (graph) => {
            jsonld.t.iterateResources(graph, (resource) => {
                const types = jsonld.r.getTypes(resource);
                if (types.indexOf(LP.JAR_TEMPLATE) !== -1) {
                    const component = parseJarTemplate(resource, graph);
                    data.jarTemplate[component["id"]] = component;
                } else if (types.indexOf(LP.REF_TEMPLATE) !== -1) {
                    const component = parseRefTemplate(resource, graph);
                    data.refTemplate[component["id"]] = component;
                }
            })
        });
    }

    function parseJarTemplate(resource, graph) {
        const keywords = jsonld.r.getPlainStrings(resource, LP.keyword);
        const type = jsonld.r.getIRIs(resource, LP.HAS_TYPE);
        const infoLink = jsonld.r.getIRI(resource, LP.HAS_INFO_LINK);
        const supportControl = getSupportControl(resource);
        const dialogs = getDialogs(resource, graph);
        const basic = parseBasicTemplate(resource);
        const search = (basic.label + "," + keywords.join(",")).toLowerCase();
        return {
            ...basic,
            ...parsePorts(resource, graph),
            "keyword": keywords,
            "type": type,
            "dialogs": dialogs,
            "isCore": true,
            "supportControl": supportControl,
            "infoLink": infoLink,
            "search": search
        }
    }

    function getSupportControl(resource) {
        const supportControl = jsonld.r.getBoolean(
            resource, LP.HAS_SUPPORT_CONTROL);
        if (supportControl === undefined) {
            return false;
        } else {
            return supportControl;
        }
    }

    function getDialogs(resource, graph) {
        const dialogs = [];
        jsonld.t.getReferences(graph, resource, LP.HAS_DIALOG).forEach(
            (dialog) => {
                dialogs.push({
                    "name": jsonld.r.getPlainStrings(dialog, LP.HAS_NAME)
                });
            });
        return dialogs;
    }

    function parsePorts(resource, graph) {
        const inputs = [];
        const outputs = [];

        let inputDataPortsCount = 0;
        let configurationPortsCount = 0;
        let taskListPortsCount = 0;

        jsonld.t.getReferences(graph, resource, LP.HAS_PORT).forEach((port) => {

            const portItem = {
                "label": jsonld.r.getPlainString(port, SKOS.PREF_LABEL),
                "binding": jsonld.r.getPlainString(port, LP.HAS_BINDING),
                "types": jsonld.r.getTypes(port),
                "content": undefined
            };

            portItem.types.forEach((item) => {
                if (item === LP.PORT) {
                    // Port type, ignore.
                } else if (item === LP.INPUT) {
                    inputs.push(portItem);
                    inputDataPortsCount++;
                } else if (item === LP.RUNTIME_CONFIGURATION) {
                    portItem["isRuntimeConfiguration"] = true;
                    inputs.push(portItem);
                    configurationPortsCount++;
                } else if (item === LP.TASK_LIST) {
                    taskListPortsCount++;
                    portItem["isTaskList"] = true;
                    inputs.push(portItem);
                } else if (item === LP.OUTPUT) {
                    outputs.push(portItem);
                } else {
                    // Define type of the port.
                    portItem["content"] = item;
                }
            });
        });

        return {
            "inputs": inputs,
            "outputs": outputs,
            "isDisplayLabels": {
                "dataInput": inputDataPortsCount > 1,
                "runtimeConfiguration": configurationPortsCount > 1,
                "taskList": taskListPortsCount > 1
            }
        }
    }

    function parseBasicTemplate(resource) {
        const id = jsonld.r.getId(resource);
        const label = jsonld.r.getPlainString(resource, SKOS.PREF_LABEL);
        const description = jsonld.r.getPlainString(
            resource, DCTERMS.DESCRIPTION);
        const color = jsonld.r.getPlainString(resource, LP.HAS_COLOR);
        return {
            "id": id,
            "label": label,
            "description": description,
            "color": color
        }
    }

    function parseRefTemplate(resource, graph) {
        const template = jsonld.r.getIRI(resource, LP.HAS_TEMPLATE);
        return {
            ...parseBasicTemplate(resource),
            "template": template,
            "isCore": false,
            "supportControl": true
        };
    }

    return {
        "parseResponse": parseResponse
    }

});