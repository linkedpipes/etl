/**
 * Holds a service that should be used to manipulate templates.
 *
 * There are two type of templates jarTemplates and refTemplates.
 *
 * All type of templates contains common generated properties:
 *  _parents - List of parent, the core template is the first.
 *  _children - Recursive list of children.
 *  _core - Reference to the core template, for core reference to it self.
 *
 * The core template contains:
 *  outputs
 *  inputs
 * instead of ports.
 *
 */

define(["jquery", "jsonld"], function (jQuery, jsonld) {
    "use strict";

    const COMPONENTS_IRI = "./resources/components";

    const LP = {
        "JarTemplate": "http://linkedpipes.com/ontology/JarTemplate",
        "RefTemplate": "http://linkedpipes.com/ontology/Template",
        "keyword": "http://linkedpipes.com/ontology/keyword",
        "color": "http://linkedpipes.com/ontology/color",
        "configuration": "http://linkedpipes.com/ontology/configurationGraph",
        "type": "http://linkedpipes.com/ontology/componentType",
        "dialog": "http://linkedpipes.com/ontology/dialog",
        "name": "http://linkedpipes.com/ontology/name",
        "port": "http://linkedpipes.com/ontology/port",
        "binding": "http://linkedpipes.com/ontology/binding",
        "template": "http://linkedpipes.com/ontology/template",
        "Input": "http://linkedpipes.com/ontology/Input",
        "Output": "http://linkedpipes.com/ontology/Output",
        "Port": "http://linkedpipes.com/ontology/Port",
        "supportControl": "http://linkedpipes.com/ontology/supportControl"
    };

    const SKOS = {
        "prefLabel": "http://www.w3.org/2004/02/skos/core#prefLabel"
    };

    const DCTERMS = {
        "description": "http://purl.org/dc/terms/description"
    };

    const DIALOG_NAME_LABEL = {
        "config": "Configuration",
        "template": "Template",
        "instance": "Inheritance"
    };

    // TODO Move to the separated module
    const i18 = {
        "str": function (value) {
            if (value === undefined) {
                return undefined;
            }
            if (Array.isArray(value)) {
                const result = [];
                value.forEach((item) => {
                    result.push(item["@value"]);
                });
                return result;
            } else if (value["@value"]) {
                return value["@value"];
            } else {
                return value;
            }
        }
    };

    /**
     * Parse and return JAR template.
     *
     * @param data
     * @param resource
     * @param graph
     */
    function parseJarTemplate(data, resource, graph) {
        const id = jsonld.r.getId(resource);
        const label = jsonld.r.getString(resource, SKOS.prefLabel);
        const description = jsonld.r.getString(resource, DCTERMS.description);
        const keyword = jsonld.r.getStrings(resource, LP.keyword);
        const color = jsonld.r.getString(resource, LP.color);
        // const configuration = jsonld.r.getIRIs(resource, LP.configuration);
        const type = jsonld.r.getIRIs(resource, LP.type);
        let supportControl =  jsonld.r.getBoolean(resource, LP.supportControl);
        if (supportControl === undefined) {
            supportControl = false;
        }

        const dialogs = [];
        jsonld.r.getReferences(graph, resource, LP.dialog).forEach((dialog) => {
            dialogs.push({
                "name": i18.str(jsonld.r.getString(dialog, LP.name))
            });
        });

        const inputs = [];
        const outputs = [];
        jsonld.r.getReferences(graph, resource, LP.port).forEach((port) => {
            const portItem = {
                "label": i18.str(jsonld.r.getString(port, SKOS.prefLabel)),
                "binding": i18.str(jsonld.r.getString(port, LP.binding)),
                "types": jsonld.r.getTypes(port),
                "content": undefined
            };
            portItem.types.forEach((item) => {
                if (item === LP.Port) {
                    return;
                } else if (item === LP.Input) {
                    inputs.push(portItem);
                } else if (item === LP.Output) {
                    outputs.push(portItem);
                } else {
                    portItem.content = item;
                }
            });
        });

        data.jarTemplate[id] = {
            "id": id,
            "label": i18.str(label),
            "description": i18.str(description),
            "keyword": keyword,
            "color": i18.str(color),
            "type": type,
            "dialogs": dialogs,
            "inputs": inputs,
            "outputs": outputs,
            "core": true,
            "supportControl" : supportControl
        };
    }

    /**
     * Parse and return reference template.
     *
     * @param data
     * @param resource
     */
    function parseRefTemplate(data, resource) {
        const id = jsonld.r.getId(resource);
        const label = jsonld.r.getString(resource, SKOS.prefLabel);
        const description = jsonld.r.getString(resource, DCTERMS.description);
        const template = jsonld.r.getIRI(resource, LP.template);
        const color = jsonld.r.getString(resource, LP.color);
        data.refTemplate[id] = {
            "id": id,
            "label": i18.str(label),
            "description": i18.str(description),
            "template": template,
            "color": i18.str(color),
            "core" : false,
            "supportControl" : true
        };
    }

    /**
     *
     * @param url
     * @param $http
     * @returns Promise with configuration graph.
     */
    function fetchConfiguration(url, $http) {
        const options = {"headers": {"Accept": "application/ld+json"}};
        return $http.get(url, options, {
            // Suppress default AngularJS conversion.
            transformResponse: (data) => {
                return data;
            }
        }).then(function (response) {
            if (response.data.length === 0) {
                return [];
            }
            // TODO Use jsonld service to get the graph here.
            return response.data[0]['@graph'];
        });
    }

    /**
     * Add generated properties to templates and construct templateList.
     *
     * @param data
     */
    function expandTemplates(data) {
        const templateList = [];
        const templateMap = {};
        // Prepare jarTemplates.
        for (let iri in data.jarTemplate) {
            if (!data.jarTemplate.hasOwnProperty(iri)) {
                continue;
            }
            const instance = data.jarTemplate[iri];
            //
            instance._parents = [];
            instance._children = [];
            instance._core = instance;
            //
            templateList.push(instance);
            templateMap[iri] = instance;
        }
        // Prepare refTemplates. To update the redTemplate we need
        // its parent to be already updated.
        let maxParentSize = 0;
        while (true) {
            let templateExpanded = false;
            let nothingToExpand = true;
            for (let iri in data.refTemplate) {
                if (!data.refTemplate.hasOwnProperty(iri)) {
                    continue;
                }
                if (templateMap[iri] !== undefined) {
                    // Already expanded.
                    continue;
                }
                nothingToExpand = false;
                // Make sure we have the instance and the parent.
                const instance = data.refTemplate[iri];
                const parent = templateMap[instance.template];
                // Check for parent.
                if (parent === undefined) {
                    continue;
                }
                templateExpanded = true;
                // We can update the template here.
                instance._parents = [];
                instance._children = [];
                Array.prototype.push.apply(instance._parents, parent._parents);
                instance._parents.push(parent);
                // Keep track of tree height.
                maxParentSize = Math.max(maxParentSize,
                    instance._parents.length);
                //
                templateList.push(instance);
                templateMap[iri] = instance;
            }
            //
            if (nothingToExpand) {
                break;
            }
            if (!templateExpanded) {
                console.error("Fail to expand all templates!");
                break;
            }
        }
        // Set children.
        for (let parentSize = 1; parentSize <= maxParentSize; parentSize++) {
            for (let i = 0; i < templateList.length; i++) {
                const template = templateList[i];
                if (template._parents.length === parentSize) {
                    // Add reference to us to out parent.
                    for (let p = 0; p < template._parents.length; p++) {
                        template._parents[p]._children.push(template);
                    }
                }
            }
        }
        // Set core template.
        for (let iri in data.jarTemplate) {
            if (!data.jarTemplate.hasOwnProperty(iri)) {
                continue;
            }
            const instance = data.jarTemplate[iri];
            for (let i = 0; i < instance._children.length; ++i) {
                instance._children[i]._core = instance;
            }
        }
        data.templateList = templateList;
    }

    function factoryFunction($http, $q) {

        const data = {
            "loaded": false,
            "jarTemplate": {},
            "refTemplate": {},
            "templateList": [],
            "config": {}
        };

        const service = {};

        service.load = (force) => {
            if (data.loaded && !force) {
                return $q.when();
            }
            return $http.get(COMPONENTS_IRI).then((response) => {
                console.time("Loading templates ...");
                // Clear caches.
                data.config = {};
                // Each component is stored in a single graph.
                response = jsonld.quads(response.data);
                response.iterateGraphs((graph) => {
                    graph = jsonld.triples(graph);
                    graph.iterate((resource) => {
                        const types = jsonld.r.getTypes(resource);
                        if (types.indexOf(LP.JarTemplate) !== -1) {
                            parseJarTemplate(data, resource, graph);
                        }
                        if (types.indexOf(LP.RefTemplate) !== -1) {
                            parseRefTemplate(data, resource);
                        }
                    });
                });
                // Expand component.
                expandTemplates(data);
                data.loaded = true;
                console.timeEnd("Loading templates ...");
            });
        };

        /**
         *
         * @param iri
         * @returns Template with given IRI.
         */
        service.getTemplate = (iri) => {
            var template = data.jarTemplate[iri];
            if (template !== undefined) {
                return template;
            }
            template = data.refTemplate[iri];
            if (template !== undefined) {
                return template;
            }
            console.warn("Missing template for: ", iri);
            return undefined;
        };

        /**
         * Try to map given template IRI to the stored templates.
         * Is used for pipeline import
         *
         * @param iri
         * @returns
         */
        service.mapToIri = function (iri) {
            const postfix = iri.substring(
                iri.indexOf('/resources/components/'));
            for (let i = 0; i < data.templateList.length; ++i) {
                let template = data.templateList[i];
                if (template.id.endsWith(postfix)) {
                    return template.id;
                }
            }
            console.warn("Can't map template:", iri);
        };

        /**
         * For given template return the core template.
         *
         * @param template
         */
        service.getCoreTemplate = (template) => {
            return template._core;
        };

        /**
         * Color that should be used by instances of this template.
         *
         * @param template
         * @returns
         */
        service.getEffectiveColor = (template) => {
            if (template.color !== undefined) {
                return template.color;
            }
            for (let i = template._parents.length - 1; i >= 0; --i) {
                const parent = template._parents[i];
                if (parent.color !== undefined) {
                    return parent.color;
                }
            }
            console.warn("Missing color for: ", template);
        };

        service.getSupportControl = (template) => {
            return template.supportControl;
        };

        /**
         * Return copy of the template that contains editable fields.
         * After update this class can be used to save the modification
         * on a template.
         *
         * @param iri
         * @returns Editable template for given IRI.
         */
        service.getEditableTemplate = (iri) => {
            const template = service.getTemplate(iri);
            if (template === undefined) {
                return undefined;
            }
            return {
                "id": template.id,
                "label": template.label,
                "description": template.description,
                "color": template.color
            };
        };

        /**
         * Return list of templates.
         *
         * @returns List.
         */
        service.getTemplatesList = () => {
            return data.templateList;
        };

        /**
         * Fetch configuration for given component.
         *
         * @param id ID of a template.
         * @returns Promise.
         */
        service.fetchConfig = (id) => {
            if (data.config[id] !== undefined) {
                return $q.when(data.config[id]);
            }
            const url = "/api/v1/components/config?iri=" + encodeURI(id);
            return fetchConfiguration(url, $http).then(function (config) {
                data.config[id] = config;
                return config;
            });
        };

        /**
         * Fetch a configuration that should be used for new instances
         * of template of given ID.
         *
         * @param id ID of a template.
         * @returns Promise.
         */
        service.fetchNewConfig = (id) => {
            const storeId = "template:" + id;
            if (data.config[storeId] !== undefined) {
                return $q.when(data.config[storeId]);
            }
            const url = "/api/v1/components/configTemplate?iri=" +
                encodeURI(id);
            return fetchConfiguration(url, $http).then(function (config) {
                data.config[storeId] = config;
                return config;
            });
        };

        /**
         * Fetch a description of the configuration.
         *
         * @param id ID of a template.
         * @returns Promise.
         */
        service.fetchConfigDesc = (id) => {
            // TODO Load only for "core" templates.
            const storeId = "desc:" + id;
            if (data.config[storeId] !== undefined) {
                return $q.when(data.config[storeId]);
            }
            const url = "/api/v1/components/configDescription?iri=" +
                encodeURI(id);
            return fetchConfiguration(url, $http).then(function (config) {
                data.config[storeId] = config;
                return config;
            });
        };

        /**
         * Fetch a configuration that represent the effective configuration
         * of a template.
         *
         * @param id
         * @returns Promise.
         */
        service.fetchEffectiveConfig = (id) => {
            const storeId = "effective:" + id;
            if (data.config[storeId] !== undefined) {
                return $q.when(data.config[storeId]);
            }
            const url = "/api/v1/components/effective?iri=" + encodeURI(id);
            return fetchConfiguration(url, $http).then(function (config) {
                data.config[storeId] = config;
                return config;
            });
        };

        /**
         * Return dialogs for given component.
         *
         * @param id
         * @param isTemplate True to return dialogs for template.
         */
        service.getDialogs = (id, isTemplate) => {
            const template = service.getTemplate(id);
            if (template === undefined) {
                console.warn('Missing dialogs for: ', id);
                return [];
            }
            // The dialogs are stored in the core template.
            const core = template._core;
            // There might be no dialogs at all.
            if (core.dialogs === undefined) {
                return [];
            }
            //
            const result = [];
            core.dialogs.forEach((dialog) => {
                const name = dialog.name;
                // Filter template/instance specific.
                if (!isTemplate && name === "template") {
                    return;
                } else if (isTemplate && name === "instance") {
                    return;
                }
                // Construct information object about the dialog.
                var baseUrl = '/api/v1/components/dialog' +
                    '?iri=' + encodeURIComponent(core.id) +
                    '&name=' + encodeURIComponent(name) +
                    '&file=';
                result.push({
                    "name": name,
                    "label": DIALOG_NAME_LABEL[name],
                    "html": baseUrl + "dialog.html",
                    "js": baseUrl + "dialog.js"
                });
            });

            result.sort(function (left, right) {
                // TODO Let dialog provide the order.
                if (left.name === "config") {
                    return false;
                }
                return left.name > right.name;
            });

            return result;
        };

        service.saveTemplate = (template, configuration) => {
            // Build the template update object.
            const updateObject = {
                "@id": template.id
            };
            updateObject[SKOS.prefLabel] = template.label;
            updateObject[LP.color] = template.color;
            updateObject[DCTERMS.description] = template.description;

            const options = {
                'transformRequest': angular.identity,
                'headers': {
                    'Content-Type': undefined,
                    'Accept': 'application/ld+json'
                }
            };

            // Save configuration.
            var configData = new FormData();
            configData.append('configuration', new Blob(
                [JSON.stringify(configuration)], {
                    type: "application/ld+json"
                }), 'configuration.jsonld');
            return $http.post('./api/v1/components/config?iri=' +
                encodeURIComponent(template.id), configData, options).then(
                () => {
                    // Save component.
                    var configData = new FormData();
                    configData.append('component', new Blob(
                        [JSON.stringify(updateObject)], {
                            type: "application/ld+json"
                        }), 'component.jsonld');
                    return $http.post('./api/v1/components/component?iri=' +
                        encodeURIComponent(template.id), configData, options);
                }).then(() => {
                // TODO Do not reload all.
                return service.load(true);
            });
        };

        return service;
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        app.factory("template.service", ["$http", "$q", factoryFunction]);
    };

});
