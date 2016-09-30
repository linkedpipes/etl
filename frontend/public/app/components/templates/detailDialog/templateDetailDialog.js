define([
    "jquery", "jsonld",
    "../detailDirective/templateDetailDirective",
    "../templateService"
], function (jQuery, jsonld, detailDirective, templateService) {
    "use strict";

    const LP = {
        "template": "http://linkedpipes.com/ontology/template",
        "color": "http://linkedpipes.com/ontology/color",
        "configurationGraph": "http://linkedpipes.com/ontology/configurationGraph"
    }

    const SKOS = {
        "prefLabel": "http://www.w3.org/2004/02/skos/core#prefLabel"
    };

    const DCTERMS = {
        "description": "http://purl.org/dc/terms/description"
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

    /**
     *
     * @param $http
     * @param template Template to edit object.
     * @param parent Parent template.
     * @param configuration
     * @return Promise with new template IRI.
     */
    function createTemplate($http, template, parent, configuration) {

        const resource = {
            "@type": ["http://linkedpipes.com/ontology/Template"]
        };
        jsonld.r.setString(resource, SKOS.prefLabel, template.label);
        jsonld.r.setString(resource, DCTERMS.description, template.description);
        jsonld.r.setIRI(resource, LP.template, parent.id);
        if (template.color !== undefined) {
            jsonld.r.setString(resource, LP.color, template.color);
        }

        // Post the data.
        const postConfiguration = {
            "transformRequest": angular.identity,
            "headers": {
                "Content-Type": undefined,
                "Accept": "application/ld+json"
            }
        };
        var data = new FormData();
        data.append("component", new Blob([JSON.stringify(resource)], {
            type: "application/ld+json"
        }), "component.jsonld");
        data.append("configuration", new Blob([JSON.stringify(configuration)], {
            type: "application/ld+json"
        }), "configuration.jsonld");
        return $http.post("./resources/components", data, postConfiguration)
        .then((response) => {
            return response.data[0]["@graph"][0]["@id"];
        });
    }

    function controller($scope, $http, $mdDialog, templateService,
                        statusService, component, template, pipeline) {

        $scope.api = {};

        $scope.templateToEdit = undefined;
        $scope.configuration = undefined;

        $scope.onSave = () => {
            // Update shared data.
            $scope.api.save();

            createTemplate($http, $scope.templateToEdit, template,
                $scope.configuration).then((iri) => {
                // TODO Move to pipeline manipulation service.
                // Update component : change parent IRI
                // and disconnect configuration as it was
                // 'moved' to a template.

                component['http://linkedpipes.com/ontology/template'] = {
                    '@id': iri
                };

                var configIRI = jsonld.r.getIRI(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                if (configIRI !== undefined) {
                    delete component['http://linkedpipes.com/ontology/configurationGraph'];
                    delete pipeline.model.graphs[configIRI];
                }

                statusService.success({
                    'title': 'Template created.'
                });

                $mdDialog.hide();
            }, () => {
                statusService.postFailed({
                    'title': "Can't create template."
                });
            });

        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        /**
         * Return component configuration or undefined if it's missing.
         *
         * @returns Can be undefined.
         */
        function getComponentConfiguration() {
            const iri = jsonld.r.getIRI(component, LP.configurationGraph);
            if (iri === undefined) {
                return undefined;
            }
            return jQuery.extend(true, [], pipeline.model.graphs[iri]);
        }

        /**
         * Must be called once all data are ready. It will initialize the
         * directive.
         */
        function initDirective() {

            $scope.api.store = {
                "template": $scope.component,
                "templateToEdit": $scope.templateToEdit,
                "configuration": $scope.configuration
            };

            if ($scope.api.load !== undefined) {
                $scope.api.load();
            }

        }

        function init() {
            // TODO Move to some instance service.
            $scope.templateToEdit = {
                "id": "",
                "label": i18.str(jsonld.r.getString(
                    component, SKOS.prefLabel)),
                "description": i18.str(jsonld.r.getString(
                    component, DCTERMS.description)),
                "color": i18.str(jsonld.r.getString(
                    component, LP.color))
            };
            // Construct a new temporary template object.
            $scope.component = jQuery.extend({}, template,
                {
                    "id": template.id,
                    "template": template.id,
                });

            $scope.configuration = getComponentConfiguration();
            if ($scope.configuration === undefined) {
                templateService.fetchNewConfig(template.id).then((config) => {
                    $scope.configuration = jQuery.extend(true, [], config);
                    initDirective();
                });
            } else {
                initDirective();
            }

        }

        templateService.load().then(init);
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        detailDirective(app);
        templateService(app);
        //
        app.controller("template.detail.dialog", ["$scope", "$http",
            "$mdDialog", "template.service", "services.status", "component",
            "template", "pipeline", controller]);
    };

});
