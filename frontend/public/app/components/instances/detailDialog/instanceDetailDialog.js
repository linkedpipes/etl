define([
    "jquery", "jsonld",
    "../detailDirective/instanceDetailDirective",
    "../../templates/templateService"
], function (jQuery, jsonld, instanceDirective, templateService) {
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
            if (value === undefined) {
                return undefined;
            }
            if (Array.isArray(value)) {
                const result = [];
                value.forEach((item) => {
                    result.push(item["@value"]);
                });
                return result;
            } else if (value["@value"] !== undefined) {
                return value["@value"];
            } else {
                return value;
            }
        }
    };

    function controller($scope, $mdDialog, component, template, pipeline,
                        templateService) {

        $scope.api = {};

        $scope.onSave = () => {
            // Update shared data.
            $scope.api.save();
            // Save changes in instance.
            jsonld.r.setStrings(component, SKOS.prefLabel,
                $scope.componentToEdit.label);
            if ($scope.componentToEdit.description === undefined ||
                $scope.componentToEdit.description === "") {
                jsonld.r.setStrings(component, DCTERMS.description,
                    undefined);
            } else {
                jsonld.r.setStrings(component, DCTERMS.description,
                    $scope.componentToEdit.description);
            }
            if ($scope.componentToEdit.color === undefined) {
                delete component[LP.color];
            } else {
                component[LP.color] = $scope.componentToEdit.color;
            }
            // Save configuration.
            var configGraph = jsonld.r.getIRI(component,
                'http://linkedpipes.com/ontology/configurationGraph');
            if (configGraph === undefined) {
                configGraph = jsonld.r.getId(component) + '/configuration';
                jsonld.r.setIRIs(component, LP.configurationGraph, configGraph);
            }
            if (!template.supportControl) {
                // Update configuration.

            }
            pipeline.model.graphs[configGraph] = $scope.configuration;
            //
            $mdDialog.hide();
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        function initDirective() {

            if (template.supportControl) {
                $scope.componentToEdit.useTemplateConfig = false;
            } else {
                // Load from configuration.
                $scope.componentToEdit.useTemplateConfig = false;
            }

            $scope.api.store = {
                "instance": $scope.componentToEdit,
                "parent": template,
                "configuration": $scope.configuration
            };

            if ($scope.api.load !== undefined) {
                $scope.api.load();
            }

        }

        function init() {

            $scope.componentToEdit = {
                "id": jsonld.r.getId(component),
                "label": i18.str(jsonld.r.getString(
                    component, SKOS.prefLabel)),
                "description": i18.str(jsonld.r.getString(
                    component, DCTERMS.description)),
                "color": i18.str(jsonld.r.getString(
                    component, LP.color))
            };

            var configIRI = jsonld.r.getIRI(component,
                'http://linkedpipes.com/ontology/configurationGraph');
            if (configIRI === undefined) {
                templateService.fetchNewConfig(template.id).then((config) => {
                    $scope.configuration = jQuery.extend(true, [], config);
                    initDirective();
                });
            } else {
                $scope.configuration = jQuery.extend(true, [],
                    pipeline.model.graphs[configIRI]);
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
        instanceDirective(app);
        templateService(app);
        //
        app.controller("instance.detail.dialog", ["$scope",
            "$mdDialog", "component", "template", "data", "template.service",
            controller]);
    };

});
