/**
 * Define a service that is used as API by the component dialogs.
 */
define([
    "jsonld",
    "./dialogManager",
    "./controlInstanceDirective/dialogControlInstanceDirective",
    "./controlTemplateDirective/dialogControlTemplateDirective",
], function (jsonld, dialogManager, instanceDirective, templateDirective) {
    "use strict";

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
                "jsonld": jsonld
            };

            // API used by host to manage this service.
            //

            service.api = {
                "setIri" : (iri) => {
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
        instanceDirective(app);
        templateDirective(app);
        //
        app.factory("template.dialog.service", [factoryFunction]);
    };
});
