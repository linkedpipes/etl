/**
 * Component designed to be used to manage content of the template
 * dialogs.
 *
 * For each "property" create object with:
 *  value - the value to show in UI
 *  templateValue - value of template if it exists
 *  hide - true if UI element should be hidden
 *  disabled - true if UI element should be read-only
 *  inherit - true if inheritance control is set to true
 *  force - true if force control is set to true
 *  label - label for UI
 *
 * Usage for X:
 *   ng-hide="dialog.X.hide"
 *   ng-model="dialog.X.value"
 *   ng-disabled="dialog.X.disabled || dialog.X.inherit"
 * Can be used only for simple values.
 *
 */
define(["jsonld"], function (jsonld) {
    "use strict";

    const LP_RESOURCE = "http://plugins.linkedpipes.com/resource/";

    const LP = {
        "Inherit": LP_RESOURCE + "configuration/Inherit",
        "Force": LP_RESOURCE + "configuration/Force",
        "InheritAndForce": LP_RESOURCE + "configuration/InheritAndForce",
        "Forced": LP_RESOURCE + "configuration/Forced",
        "None": LP_RESOURCE + "configuration/None"
    };

    // TODO Move to the separated module
    const i18 = {
        "str": function (value) {
            if (Array.isArray(value)) {
                const result = [];
                value.forEach((item) => {
                    result.push(item[""]);
                });
                return result;
            } else {
                return value[""];
            }
        },
        "value": function (string) {
            return {
                "@value": string
            };
        }
    };

    function iriToControl(iri) {
        switch (iri) {
            case LP.Inherit:
                return {
                    "inherit": true,
                    "force": false,
                    "forced": false
                };
            case LP.Force:
                return {
                    "inherit": false,
                    "force": true,
                    "forced": false
                };
            case LP.InheritAndForce:
                return {
                    "inherit": true,
                    "force": true,
                    "forced": false
                };
            case LP.Forced:
                return {
                    "forced": true
                };
            case LP.None:
                return {
                    "inherit": false,
                    "force": false,
                    "forced": false
                };
            default:
                return {
                    "inherit": false,
                    "force": false,
                    "forced": false
                };
        }
    }

    function controlToIri(inherit, force) {
        if (inherit) {
            if (force) {
                return LP.InheritAndForce;
            } else {
                return LP.Inherit;
            }
        } else {
            if (force) {
                return LP.Force;
            } else {
                return LP.None;
            }
        }
    }

    // TODO Add support for lists (strings, string with language, integers .. )

    function loadValue(desc, ns, resource) {
        switch(desc.$type) {
            case "str":
                return i18.str(jsonld.r.getString(
                    resource,ns + desc.$property));
            case "int":
                return jsonld.r.getInteger(resource,ns + desc.$property);
            case "bool":
                return jsonld.r.getBoolean(resource,ns + desc.$property);
            case "iri":
                return jsonld.r.getIRI(resource,ns + desc.$property);
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    function saveValue(desc, ns, resource, value) {
        switch(desc.$type) {
            case "str":
                jsonld.r.setString(resource,ns + desc.$property,
                    i18.value(value));
                break;
            case "int":
                jsonld.r.setInteger(resource,ns + desc.$property, value);
                break;
            case "bool":
                jsonld.r.setBoolean(resource,ns + desc.$property, value);
                break;
            case "iri":
                jsonld.r.setIRI(resource,ns + desc.$property, value);
                break;
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    function loadTemplate(desc, dialog, instanceConfig, instance,
                          templateConfig, template) {
        const ns = (desc.$namespace === undefined) ? "" : desc.$namespace;

        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            const item = desc[key];
            //
            const instanceValue = loadValue(item, ns, instance);
            const instanceControl = iriToControl(
                jsonld.r.getIRI(instance,ns + item.$control));
            const templateValue = loadValue(item, ns, template);
            const templateControl = iriToControl(
                jsonld.r.getIRI(template, ns + item.$control));
            //
            dialog[key] = {
                "value": instanceValue,
                "templateValue": templateValue,
                "forced" : templateControl.forced,
                "hide": templateControl.forced,
                "disabled": false,
                "inherit": instanceControl.inherit,
                "force": instanceControl.force,
                "label": item.$label
            };
        }
    }

    function loadCoreTemplate(desc, dialog, instanceConfig, instance) {
        console.log('   !!! MISSING IMPLEMENTATION !!!')
    }

    /**
     * Load given configurations into the dialog.
     *
     * @param desc
     * @param dialog
     * @param instanceConfig
     * @param templateConfig
     */
    function load(desc, dialog, instanceConfig, templateConfig) {
        const ns = (desc.$namespace === undefined) ? "" : desc.$namespace;

        instanceConfig = jsonld.triples(instanceConfig);
        const instance = instanceConfig.getResource(
            instanceConfig.findByType(ns + desc.$type));

        if (templateConfig === undefined) {
            loadCoreTemplate(desc, dialog, instanceConfig, instance);
            return;
        }

        templateConfig = jsonld.triples(templateConfig);
        const template = templateConfig.getResource(
            templateConfig.findByType(ns + desc.$type));

        loadTemplate(desc, dialog, instanceConfig, instance,
            templateConfig, template);

        // console.debug("LOAD");
        // console.debug("instance: ", instance);
        // console.debug("template: ", template);
        // console.debug("desc:", desc);
        // console.debug(" ->  ", dialog);
    }

    /**
     * Save content from dialog to configuration.instance.
     *
     * @param desc
     * @param dialog
     * @param iri
     * @param configuration
     */
    function save(desc, dialog, iri, configuration) {
        const ns = (desc.$namespace === undefined) ? "" : desc.$namespace;

        // Construct the configuration object.
        const resource = {
            '@id': iri + "/configuration",
            "@type": [ns + desc.$type]
        };

        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            const item = desc[key];
            if (dialog[key].forced) {
                jsonld.r.setIRI(resource, ns + item.$control, LP.Forced);
                continue;
            }

            saveValue(item, ns, resource, dialog[key].value);
            jsonld.r.setIRI(resource, ns + item.$control,
                controlToIri(dialog[key].inherit, dialog[key].force));
        }

        console.debug("SAVE", dialog, "->", resource);

        // Perform in-place modification of the array.
        configuration.length = 0
        configuration.push(resource);
    }

    /**
     *
     * @param dialogService Dialog service.
     * @param description Description given by dialog.
     * @param dialog The dialog object given by dialog.
     * @returns
     */
    function factoryFunction (dialogService, description, dialog) {
        const service = {
            "load": () => {
                load(description, dialog, dialogService.config.instance,
                    dialogService.config.template);
            },
            "save": () => {
                save(description, dialog, dialogService.iri,
                    dialogService.config.instance);
            }
        };

        return service;
    }

    return factoryFunction;

})
