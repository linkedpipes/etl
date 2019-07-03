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
define(["@client/app-service/jsonld/jsonld"], function (jsonld) {
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

    /**
     * Convert IRI to control object.
     */
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

    /**
     * Convert control properties to IRI.
     */
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

    /**
     * Return simple value.
     */
    function loadValue(desc, resource) {
        switch (desc.$type) {
            case "date":
                return jsonld.r.getDates(resource, desc.$property);
            case "str":
                return i18.str(jsonld.r.getStrings(resource, desc.$property));
            case "str-lang":
                return jsonld.r.getStrings(resource, desc.$property);
            case "int":
                return jsonld.r.getIntegers(resource, desc.$property);
            case "bool":
                return jsonld.r.getBooleans(resource, desc.$property);
            case "iri":
                return jsonld.r.getIRIs(resource, desc.$property);
            case "value":
                return jsonld.r.getValue(resource, desc.$property);
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    /**
     * Store simple value into the given resource.
     */
    function saveValue(desc, resource, value) {
        switch (desc.$type) {
            case "date":
                jsonld.r.setDates(resource, desc.$property, value);
                break;
            case "str":
            case "str-lang":
                jsonld.r.setStrings(resource, desc.$property, value);
                break;
            case "int":
                jsonld.r.setIntegers(resource, desc.$property, value);
                break;
            case "bool":
                jsonld.r.setBooleans(resource, desc.$property, value);
                break;
            case "iri":
                jsonld.r.setIRIs(resource, desc.$property, value);
                break;
            case "value":
                jsonld.r.setValue(resource, desc.$property, value);
                break;
            default:
                console.error("Unknown type for: ", desc);
        }
    }

    /**
     * Some value may be undefined (ie. strings, booleans), this may case
     * problems as for example undefined is equal to false (for UI checkbox)
     *
     * For this reason we check for undefined values and replace them
     * with default.
     */
    function replaceUndefined(desc, value) {
        if (desc.$array || value !== undefined) {
            return value;
        }

        if (desc.$type === "bool") {
            return false;
        }

        return value;
    }

    /**
     * Load given RDF instance into target object.
     *
     * @param desc Description.
     * @param instance RDF resource.
     * @param instanceTriples RDF triples.
     * @param target Target to load into.
     */
    function loadObject(desc, instance, triples, target) {
        // Object level control.
        if (desc.$control !== undefined) {
            const instanceControl = iriToControl(
                jsonld.r.getIRI(instance, desc.$control));
            target.$control = {
                "inherit": instanceControl.inherit,
                "force": instanceControl.force,
                "forced": instanceControl.forced
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
            const descItem = desc[key];
            // Load values from instance and template.
            let instanceValue;
            if (descItem.$object === undefined) {
                instanceValue = replaceUndefined(descItem,
                    select(loadValue(descItem, instance), descItem));
            } else {
                const objects = [];
                const iris = jsonld.r.getIRIs(instance, descItem.$property);
                for (let i = 0; i < iris.length; ++i) {
                    const iri = iris[i];
                    const objectInstance = jsonld.t.getResource(triples, iri);
                    const object = {};
                    //
                    loadObject(descItem.$object, objectInstance, triples,
                        object);
                    objects.push(object);
                }
                instanceValue = select(objects, descItem);
            }
            if (descItem.$onLoad !== undefined) {
                instanceValue = descItem.$onLoad(instanceValue);
            }
            if (descItem.$control === undefined) {
                target[key] = {
                    "value": instanceValue,
                    "inherit": false,
                    "force": false,
                    "forced": false
                };
            } else {
                const instanceControl = iriToControl(
                    jsonld.r.getIRI(instance, descItem.$control));
                // Create dialog representation.
                target[key] = {
                    "value": instanceValue,
                    "inherit": instanceControl.inherit,
                    "force": instanceControl.force,
                    "forced": instanceControl.forced
                };
            }
        }
    }

    /**
     *
     * @param desc Object description class.
     * @param toSave to save, must be a an object.
     * @param graph RDF object.
     * @param iri The base IRI for the object.
     */
    function saveObject(desc, toSave, graph, iri) {
        const instance = {
            "@id": iri,
            "@type": [desc.$type]
        };
        // Check object control.
        if (desc.$control !== undefined && toSave.$cotrol !== undefined &&
            toSave.$control.inherit !== undefined &&
            toSave.$control.force !== undefined) {
            jsonld.r.setIRIs(instance, desc.$control,
                controlToIri(toSave.$control.inherit, toSave.$control.force));
        }
        // Save object data properties.
        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            const descItem = desc[key];
            let toSaveItem = toSave[key];
            // toSaveItem can be a simple object, created outside the dialog.
            // In that case we create a simple wrap.
            if (!(toSaveItem instanceof Object)) {
                toSaveItem = {
                    "value": toSaveItem
                };
            }
            // Get value to save, the value can be transformed before saving.
            let valueToSave;
            if (descItem.$onSave !== undefined) {
                valueToSave = descItem.$onSave(toSaveItem.value);
            } else {
                valueToSave = toSaveItem.value;
            }
            // Save value.
            if (descItem.$object === undefined) {
                saveValue(descItem, instance, valueToSave);
            } else {
                // Save objects.
                if (!Array.isArray(valueToSave)) {
                    valueToSave = [valueToSave];
                }
                const iris = [];
                for (let i = 0; i < valueToSave.length; ++i) {
                    const newIri = iri + "/" + i;
                    saveObject(descItem.$object, valueToSave[i], graph, newIri);
                    iris.push(newIri);
                }
                jsonld.r.setIRIs(instance, descItem.$property, iris);
            }
            // Save control.
            if (descItem.$control !== undefined &&
                toSaveItem.inherit !== undefined &&
                toSaveItem.force !== undefined) {
                jsonld.r.setIRIs(instance, descItem.$control,
                    controlToIri(toSaveItem.inherit, toSaveItem.force));
            }
        }
        //
        graph.push(instance);
    }

    /**
     * Perform in-place preparation of the description object. Can be
     * called multiple times on the same object.
     */
    function prepareDescription(desc, ns, autoControl) {
        if (desc.$decorated) {
            return;
        } else {
            desc.$decorated = true;
        }
        // Load optional values.
        if (ns === undefined) {
            ns = (desc.$namespace === undefined) ? "" : desc.$namespace;
        }
        if (autoControl === undefined) {
            autoControl = false;
        }
        // Load configuration options.
        if (desc.$options !== undefined) {
            autoControl = desc.$options.$control === "auto";
        }
        // Update object properties.
        if (desc.$class !== undefined) {
            desc.$class = ns + desc.$class;
        }
        if (desc.$control !== undefined) {
            desc.$control = ns + desc.$control;
        }
        // The type may be set as an absolute IRI.
        if (desc.$type !== undefined && desc.$type.indexOf("http://") === -1) {
            desc.$type = ns + desc.$type;
        }
        // Iterate over resources.
        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            // Update all other.
            const item = desc[key];
            if (item.$property === undefined) {
                item.$property = ns + key;
            } else {
                item.$property = ns + item.$property;
            }
            if (item.$control === undefined) {
                if (autoControl) {
                    item.$control = item.$property + "Control";
                }
            } else {
                item.$control = ns + item.$control;
            }
            // Check for objects.
            if (item.$object !== undefined) {
                // Objects does not have controls.
                prepareDescription(item.$object, ns, false);
            }
        }
    }

    /**
     * Merge instance and template class into a dialog.
     *
     * @param desc
     * @param instance
     * @param template
     * @param dialog
     */
    function merge(desc, instance, template, dialog) {
        // Control from instance.
        if (desc.$control !== undefined) {
            dialog.$control = {
                "forced": instance.$control.forced,
                "hide": instance.$control.forced,
                "disabled": false,
                "inherit": template.$control.inherit,
                "force": template.$control.force
            };
        }
        for (let key in desc) {
            if (!desc.hasOwnProperty(key)) {
                continue;
            }
            if (key.startsWith("$")) {
                continue;
            }
            const descItem = desc[key];
            const instanceValue = instance[key];
            const templateValue = template[key];
            // TODO Add support for objects : descItem.$object === undefined
            dialog[key] = {
                "value": instanceValue.value,
                "templateValue": templateValue.value,
                "forced": templateValue.forced,
                "hide": templateValue.forced,
                "disabled": false,
                "inherit": instanceValue.inherit,
                "force": instanceValue.force,
                // From control.
                "label": descItem.$label,
                "controlled": descItem.$control !== undefined
            };
        }
    }

    /**
     * Load given configurations into the dialog object.
     *
     * @param dialog The output object.
     */
    function load(desc, dialog, instanceConfig, templateConfig) {
        prepareDescription(desc);

        const instanceResource =
            jsonld.t.getResourceByType(instanceConfig, desc.$type);
        const instance = {};
        loadObject(desc, instanceResource, instanceConfig, instance);

        if (templateConfig === undefined) {
            console.error("Core templates are not supported.");
            return;
        }

        const templateResource =
            jsonld.t.getResourceByType(templateConfig, desc.$type);
        const template = {};
        loadObject(desc, templateResource, templateConfig, template);

        merge(desc, instance, template, dialog);
    }

    /**
     * Save dialog object to configuration. Given configuration
     * must be an array. All existing data in configuration are lost.
     */
    function save(desc, dialog, iri, configuration) {
        prepareDescription(desc);
        configuration.length = 0;
        saveObject(desc, dialog, configuration, iri + "/configuration");
    }

    /**
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
