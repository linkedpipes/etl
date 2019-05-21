((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
            "./template-loader",
            "./template-expand"
        ], definition);
    }
})((vocabulary, jsonld, loader, expander) => {

    const LP = vocabulary.LP;
    const SKOS = vocabulary.SKOS;
    const DCTERMS = vocabulary.DCTERMS;

    const DIALOG_LABELS = {
        "config": "Configuration",
        "template": "Template",
        "instance": "Inheritance"
    };

    const serviceData = {
        "loaded": false,
        "jarTemplate": {},
        "refTemplate": {},
        "templateList": [],
        "cache": {}
    };

    let $q;
    let $http;

    function service(_$q, _$http) {
        $q = _$q;
        $http = _$http;

        this.load = load;
        this.forceLoad = forceLoad;
        this.getTemplate = getTemplate;
        this.getCoreTemplate = getCoreTemplate;
        this.getEffectiveColor = getEffectiveColor;
        this.getSupportControl = isSupportControl; // TODO Rename
        this.getEditableTemplate = getEditableTemplate;
        this.getTemplatesList = getTemplates;
        this.fetchConfig = fetchTemplateConfig; // TODO Rename
        this.fetchNewConfig = fetchConfigForNewInstance; // TODO Rename
        this.fetchConfigDesc = fetchConfigDescription; // TODO Rename
        this.fetchEffectiveConfig = fetchEffectiveConfig;
        this.delete = deleteTemplate;
        this.getDialogs = getDialogs;
        this.saveTemplate = updateTemplate; // TODO Rename
        this.getUsage = getUsage;
    }

    service.$inject = ["$q", "$http"];

    function load() {
        if (serviceData.loaded) {
            return $q.when();
        }
        return forceLoad();
    }

    function forceLoad() {
        serviceData.loaded = false;
        return $http.get("resources/components").then((response) => {
            console.time("Loading templates");
            clearData(serviceData);
            loader.parseResponse(serviceData, response);
            expander.expandTemplates(serviceData);
            serviceData.loaded = true;
            console.timeEnd("Loading templates");
        })
    }

    function clearData(data) {
        data.jarTemplate = {};
        data.refTemplate = {};
        data.cache = {};
    }

    function getTemplate(iri) {
        let template = serviceData.jarTemplate[iri];
        if (template !== undefined) {
            return template;
        }
        template = serviceData.refTemplate[iri];
        if (template !== undefined) {
            return template;
        }
        console.warn("Missing template for: ", iri);
        return undefined;
    }

    function getCoreTemplate(template) {
        if (template.isCore) {
            return template;
        }
        if (template.isInvalid) {
            return undefined;
        }
        if (template._coreReference === undefined) {
            // It's probably IRI.
            template = getTemplate(template);
        }
        return template._coreReference;
    }

    function getEffectiveColor(template) {
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
    }

    function isSupportControl(template) {
        return template.supportControl;
    }

    /**
     * Return object with template editable properties.
     */
    function getEditableTemplate(iri) {
        const template = getTemplate(iri);
        if (template === undefined) {
            return undefined;
        }
        return {
            "id": template.id,
            "label": template.label,
            "description": template.description,
            "color": template.color
        };
    }

    function getTemplates() {
        return serviceData.templateList;
    }

    /**
     * Configuration of a template instance.
     */
    function fetchTemplateConfig(iri) {
        const url = "./api/v1/components/config?iri=" + encodeURI(iri);
        return fetchJsonLd(iri, url);
    }

    function fetchJsonLd(id, url) {
        if (serviceData.cache[id] !== undefined) {
            return $q.when(serviceData.cache[id]);
        }
        const options = {
            "headers": {
                "Accept": "application/ld+json"
            }
        };
        return $http.get(url, options, {
            // Suppress default AngularJS conversion.
            "transformResponse": (data) => data
        }).then((response) => {
            let graph;
            if (response.data.length === 0) {
                graph = [];
            } else {
                graph = jsonld.q.getGraph(response.data, null);
            }
            serviceData.cache[id] = graph;
            return graph;
        });
    }

    /**
     * Configuration used for new instances of this template.
     */
    function fetchConfigForNewInstance(iri) {
        const id = "new:" + iri;
        const url = "./api/v1/components/configTemplate?iri=" + encodeURI(iri);
        return fetchJsonLd(id, url);
    }

    function fetchConfigDescription(iri) {
        // Fetch from core templates only, we can ask directly,
        // but this save some memory and traffic.
        const core = getCoreTemplate(iri);
        const id = "desc:" + core.id;
        const url = "./api/v1/components/configDescription?iri=" +
            encodeURI(core.id);
        return fetchJsonLd(id, url);
    }

    /**
     * Configuration of a template instance, that should be used as a parent
     * configuration in the dialog.
     */
    function fetchEffectiveConfig(iri) {
        const id = "effective:" + iri;
        const url = "api/v1/components/effective?iri=" + encodeURI(iri);
        return fetchJsonLd(id, url);
    }

    function deleteTemplate(iri) {
        return $http({
            "method": "DELETE",
            "url": iri
        }).then(() => {
            // TODO Update only affected templates
            return forceLoad(true);
        });
    }

    function getDialogs(iri, forTemplate) {
        const template = getTemplate(iri);
        if (template === undefined) {
            return [];
        }
        // Dialogs are stored with the core template.
        const core = template._coreReference;
        if (core.dialogs === undefined) {
            return [];
        }
        const dialogs = [];
        core.dialogs.forEach((dialog) => {
            const name = dialog.name;
            // TODO Use contains instead of equal?
            // Filter dialogs for template or instance.
            if (!forTemplate && name === "template") {
                return;
            } else if (forTemplate && name === "instance") {
                return;
            }
            //
            const baseUrl = 'api/v1/components/dialog' +
                '?iri=' + encodeURIComponent(core.id) +
                '&name=' + encodeURIComponent(name) +
                '&file=';
            dialogs.push({
                "name": name,
                "label": getDialogLabel(name),
                "html": baseUrl + "dialog.html",
                "js": baseUrl + "dialog.js"
            });
        });
        orderDialogs(dialogs);
        return dialogs;
    }

    function getDialogLabel(name) {
        return DIALOG_LABELS[name];
    }

    function orderDialogs(dialogs) {
        dialogs.sort((left, right) => {
            if (left.name === "config") {
                return false;
            }
            return left.name > right.name;
        });
    }

    function updateTemplate(template, configuration) {
        return saveConfiguration(template, configuration)
            .then(() => saveTemplate(template))
            .then(() => {
                // TODO Update only affected templates.
                // Update single template.
                // Drop configurations of children.
                forceLoad();
            })
    }

    function saveConfiguration(template, configuration) {
        const form = new FormData();
        form.append('configuration', new Blob(
            [JSON.stringify(configuration)],
            {"type": "application/ld+json"}),
            'configuration.jsonld');
        const url = './api/v1/components/config?iri=' +
            encodeURIComponent(template.id);
        return $http.post(url, form, getPostJsonLdOptions());
    }

    function getPostJsonLdOptions() {
        return {
            "transformRequest": angular.identity,
            "headers": {
                "Content-Type": undefined,
                "Accept": "application/ld+json"
            }
        };
    }

    function saveTemplate(template) {
        const updateRequestContent = buildTemplateUpdate(template);
        const form = new FormData();
        form.append("component", new Blob(
            [JSON.stringify(updateRequestContent)],
            {"type": "application/ld+json"}),
            "component.jsonld");
        const url = "./api/v1/components/component?iri=" +
            encodeURIComponent(template.id);
        return $http.post(url, form, getPostJsonLdOptions());
    }

    function buildTemplateUpdate(template) {
        const result = {
            "@id": template.id
        };
        result[SKOS.PREF_LABEL] = template.label;
        result[LP.HAS_COLOR] = template.color;
        result[DCTERMS.DESCRIPTION] = template.description;
        return result;
    }

    /**
     * Return usage of the template and all its descendants in pipelines.
     */
    function getUsage(iri) {
        const url = "api/v1/usage?iri=" + encodeURI(iri);
        const options = {"headers": {"Accept": "application/ld+json"}};
        return $http.get(url, options).then((response) => {
            const pipelines = {};
            const templates = {};
            jsonld.q.iterateResources(response.data, (resource) => {
                const types = jsonld.r.getTypes(resource);
                if (types.indexOf(LP.PIPELINE) !== -1) {
                    getUsageOnPipeline(resource, pipelines);
                }
                if (types.indexOf(LP.TEMPLATE) !== -1) {
                    getUsageOnComponent(resource, pipelines, templates);
                }
            });
            addTemplatesToPipelines(templates, pipelines);
            return pipelines;
        });
    }

    function getUsageOnPipeline(resource, pipelines) {
        const id = jsonld.r.getId(resource);
        const label = jsonld.r.getPlainString(resource, SKOS.PREF_LABEL);
        pipelines[id] = {
            "label" : label,
            "templates" : []
        };
    }

    function getUsageOnComponent(resource, pipelines, templates) {
        const id = jsonld.r.getId(resource);
        const usedIn = jsonld.r.getIRIs(item, LP.HAS_USED_IN);
        templates[id] = {
            "template" : service.getTemplate(id),
            "pipelines" : usedIn
        };
    }

    function addTemplatesToPipelines(templates, pipelines) {
        for (let key in templates) {
            if (!templates.hasOwnProperty(key)) {
                continue;
            }
            const template = templates[key];
            for (let index = 0; index < template.pipelines.length; ++index) {
                const iri = template.pipelines[index];
                pipelines[iri].templates.push({
                    "id" : template.template.id,
                    "label" : template.template.label
                });
            }
        }
    }

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("template.service", service);
    }

});