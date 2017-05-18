/**
 * Define a service that is used as API by the component dialogs.
 */
define([
    "jsonld",
    "./dialogManager",
    "./controlConfigDirective/controlConfigDirective",
    "./controlInstanceDirective/dialogControlInstanceDirective",
    "./controlTemplateDirective/dialogControlTemplateDirective",
], function (jsonld, dialogManager, configDirective,
             instanceDirective, templateDirective) {
    "use strict";

    const LP_RESOURCE = "http://plugins.linkedpipes.com/resource/";

    const LP = {
        "Inherit": LP_RESOURCE + "configuration/Inherit",
        "Force": LP_RESOURCE + "configuration/Force",
        "InheritAndForce": LP_RESOURCE + "configuration/InheritAndForce",
        "Forced": LP_RESOURCE + "configuration/Forced",
        "None": LP_RESOURCE + "configuration/None"
    };    
    
    function factoryFunction() {

        /**
         * Create and return service object instance.
         */
        function createService() {

            const service = {
                /**
                 * IRI of the component dialogs.
                 */
                "iri": undefined,
                /**
                 * Holds configuration object, used to communicate
                 * configuration with the dialog.
                 */
                "config": {
                    /**
                     * Configuration of this component.
                     */
                    "instance": [],
                    /**
                     * An effective template configuration. Do not modify!
                     */
                    "template": []
                },
                /**
                 * A callback function dialog must implement.
                 * Upon call of this function the config.instance must
                 * be updated and ready to be saved as the configuration.
                 */
                "onStore": undefined,
            };

            // Contains versioned advanced services.
            //

            service.v0 = {
                "iriToControl": function (iri) {
                    switch (iri) {
                        case LP.Inherit:
                            return {
                                'inherit': true,
                                'force': false,
                                'forced': false
                            }
                        case LP.Force:
                            // This apply for templates.
                            return {
                                'inherit': false,
                                'force': true,
                                'forced': false
                            }
                        case LP.InheritAndForce:
                            // This apply for templates.
                            return {
                                'inherit': true,
                                'force': true,
                                'forced': false
                            }
                        case LP.Forced:
                            return {
                                'forced': true
                            }
                        case LP.None:
                            return {
                                'inherit': false,
                                'force': false,
                                'forced': false
                            };
                        default:
                            return {
                                'inherit': false,
                                'force': false,
                                'forced': false
                            };
                    }
                },
                "controlToIri": function (control) {
                    if (control.forced) {
                        return LP.Forced;
                    }
                    if (control.inherit) {
                        if (control.force) {
                            return LP.InheritAndForce;
                        } else {
                            return LP.Inherit;
                        }
                    } else {
                        if (control.force) {
                            return LP.Force;
                        } else {
                            return LP.None;
                        }
                    }
                }
            };

            service.v1 = {
                /**
                 * Create and return dialog manager.
                 *
                 * @param description Description of the dialog entity.
                 * @param dialog Target dialog entity.
                 */
                "manager": (description, dialog) => {
                    return dialogManager(service, description, dialog);
                },
                "jsonld": jsonld,
                "fnc": {
                    "removeEmptyIri" : function(values) {
                        const output = [];
                        values.forEach((item) => {
                            const iri = item["@id"];
                            if (iri === undefined || iri === "") {
                                return;
                            }
                            output.append({"@id" : iri});
                        });
                        return output;
                    }
                }
            };

            // API used by host to manage this service.
            //

            service.api = {
                "setIri": (iri) => {
                    service.iri = iri;
                },
                "setInstanceConfig": (config) => {
                    service.config.instance = config;
                },
                "setTemplateConfig": (config) => {
                    service.config.template = config;
                },
                "getInstanceConfig": () => {
                    return service.config.instance;
                }
            };

            return service;
        }

        return {
            "new": createService
        };
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        configDirective(app);
        instanceDirective(app);
        templateDirective(app);
        //
        app.factory("template.dialog.service", [factoryFunction]);
    };
});
