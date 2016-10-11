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

    function select(value, desc) {
        if (desc.$array) {
            return value;
        } else {
            if (value.length > 0) {
                return value[0];
            } else {
                return undefined;
            }
        }
    }

    // TODO Add support for lists (strings, string with language, integers .. )

    function loadValue(desc, ns, resource) {
        switch (desc.$type) {
            case "date":
                return jsonld.r.getDates(resource, ns + desc.$property);
            case "str":
                return i18.str(jsonld.r.getStrings(resource,
                    ns + desc.$property));
            case "int":
                return jsonld.r.getIntegers(resource, ns + desc.$property);
            case "bool":
                return jsonld.r.getBooleans(resource, ns + desc.$property);
            case "iri":
                return jsonld.r.getIRIs(resource, ns + desc.$property);
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    function saveValue(desc, ns, resource, value) {
        switch (desc.$type) {
            case "date":
                jsonld.r.setDates(resource, ns + desc.$property, value);
                break;
            case "str":
                jsonld.r.setStrings(resource, ns + desc.$property, value);
                break;
            case "int":
                jsonld.r.setIntegers(resource, ns + desc.$property, value);
                break;
            case "bool":
                jsonld.r.setBooleans(resource, ns + desc.$property, value);
                break;
            case "iri":
                jsonld.r.setIRIs(resource, ns + desc.$property, value);
                break;
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    function loadTemplate(desc, dialog, instanceConfig, instance,
                          templateConfig, template) {
        const ns = (desc.$namespace === undefined) ? "" : desc.$namespace;
        // Load dialog options.
        let autoControl = false;
        if (desc.$options !== undefined) {
            autoControl = desc.$options.$control === "auto";
            console.log('auto control', autoControl);
        }
        // Check global control (dialog level).
        if (desc.$control !== undefined) {
            const instanceControl = iriToControl(
                jsonld.r.getIRI(instance, ns + desc.$control));
            const templateControl = iriToControl(
                jsonld.r.getIRI(template, ns + desc.$control));
            //
            dialog.$control = {
                "forced": templateControl.forced,
                "hide": templateControl.forced,
                "disabled": false,
                "inherit": instanceControl.inherit,
                "force": instanceControl.force
            };
        }
        //
        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            // Part of the dialog configuration.
            const item = desc[key];
            if (item.$property === undefined) {
                item.$property = key;
            }
            if (item.$control === undefined && autoControl) {
                item.$control = item.$property + "Control";
            }
            //
            let instanceValue = select(loadValue(item, ns, instance), item);
            let templateValue = select(loadValue(item, ns, template), item);
            if (item.onLoad !== undefined) {
                instanceValue = item.$onLoad(instanceValue);
                templateValue = item.$onLoad(templateValue);
            }
            let instanceControl;
            let templateControl;
            if (item.$control === undefined) {
                instanceControl = {
                    "inherit": false,
                    "force": false
                };
                templateControl = {
                    "forced": false
                };
            } else {
                instanceControl = iriToControl(
                    jsonld.r.getIRI(instance, ns + item.$control));
                templateControl = iriToControl(
                    jsonld.r.getIRI(template, ns + item.$control));
            }
            //
            dialog[key] = {
                "value": instanceValue,
                "templateValue": templateValue,
                "forced": templateControl.forced,
                "hide": templateControl.forced,
                "disabled": false,
                "inherit": instanceControl.inherit,
                "force": instanceControl.force,
                "label": item.$label,
                "controlled": item.$control !== undefined
            };
        }
        console.log("template", template);
        console.log("dialog", dialog);
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
            console.error("Core templates are not supported.")
            return;
        }
        templateConfig = jsonld.triples(templateConfig);
        const template = templateConfig.getResource(
            templateConfig.findByType(ns + desc.$type));
        loadTemplate(desc, dialog, instanceConfig, instance,
            templateConfig, template);
    }

    /**
     * Save content from dialog to configuration.instance.
     *
     * @param desc Must be used for loading first.
     * @param dialog
     * @param iri
     * @param configuration
     */
    function save(desc, dialog, iri, configuration) {
        const ns = (desc.$namespace === undefined) ? "" : desc.$namespace;
        // Construct the configuration object.
        const resource = {
            "@id": iri + "/configuration",
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
            if (dialog[key].forced && item.$control !== undefined) {
                jsonld.r.setIRI(resource, ns + item.$control, LP.Forced);
                continue;
            }
            let value = dialog[key].value;
            if (item.$onSave !== undefined) {
                value = item.$onSave(value);
            }
            saveValue(item, ns, resource, value);
            if (item.$control !== undefined) {
                jsonld.r.setIRIs(resource, ns + item.$control,
                    controlToIri(dialog[key].inherit, dialog[key].force));
            }
        }
        // Save dialog configuration.
        if (desc.$control !== undefined) {
            jsonld.r.setIRIs(resource, ns + desc.$control,
                controlToIri(dialog.$control.inherit, dialog.$control.force));
        }
        // Perform in-place modification of the array.
        configuration.length = 0;
        configuration.push(resource);
    }

    /**
     *
     * @param dialogService Dialog service.
     * @param description Description given by dialog.
     * @param dialog The dialog object given by dialog.
     * @returns
     */
    return function factoryFunction(dialogService, description, dialog) {
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
    };
});
