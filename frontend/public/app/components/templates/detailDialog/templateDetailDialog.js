define([
    "jquery", "jsonld",
    "../detailDirective/templateDetailDirective",
    "../templateService"
], function (jQuery, jsonld, detailDirective, templateService) {
    "use strict";

    const LP = {
        "template": "http://linkedpipes.com/ontology/template",
        "color": "http://linkedpipes.com/ontology/color",
        "configurationGraph": "http://linkedpipes.com/ontology/configurationGraph",
        "control" : "http://plugins.linkedpipes.com/ontology/configuration/control"
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
            } else if (value["@value"]) {
                return value["@value"];
            } else {
                return value;
            }
        }
    };

    /**
     *
     * @param $http
     * @param template Template to edit object.
     * @param parent Parent template.
     * @param configuration
     * @param templateService
     * @return Promise with new template IRI.
     */
    function createTemplate($http, template, parent, configuration,
                            templateService) {

        // TODO Move to the templateService

        const resource = {
            "@type": ["http://linkedpipes.com/ontology/Template"]
        };
        jsonld.r.setStrings(resource, SKOS.prefLabel, template.label);
        jsonld.r.setStrings(resource, DCTERMS.description, template.description);
        jsonld.r.setIRIs(resource, LP.template, parent.id);
        if (template.color !== undefined) {
            jsonld.r.setStrings(resource, LP.color, template.color);
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
            return templateService.load(true).then(() => {
                return response.data[0]["@graph"][0]["@id"];
            });
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

            console.log('COMPONENT SAVED');

            createTemplate($http, $scope.templateToEdit, template,
                $scope.configuration, templateService).then((iri) => {
                // Update template.
                component['http://linkedpipes.com/ontology/template'] = [{
                    '@id': iri
                }];
                // Update current configuration, we know that the configuration
                // is the same. So we set all controls to inherit.
                let configIRI = jsonld.r.getIRI(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                if (configIRI === undefined) {
                    configIRI = jsonld.r.getId(component) + "/configuration";
                    jsonld.r.setIRIs(component,
                        'http://linkedpipes.com/ontology/configurationGraph',
                        configIRI);
                    // Use configuration from template.
                    // TODO This is fallback for older version of pipelines.
                    pipeline.model.graphs[configIRI] =
                        jQuery.extend(true, {} , $scope.configuration);
                }
                // Update configuration.
                templateService.fetchConfigDesc(iri).then((description) => {
                    const config = pipeline.model.graphs[configIRI];
                    // TODO Move this into separated service.
                    const controlProperties = [];
                    jsonld.t.iterateResources(description, (resource) => {
                        const control = jsonld.r.getIRI(resource, LP.control);
                        if (control !== undefined) {
                            controlProperties.push(control);
                        }
                    });
                    jsonld.t.iterateResources(config, (resource) => {
                        for (let key in resource) {
                            if (!resource.hasOwnProperty(key)) {
                                continue;
                            }
                            if (controlProperties.indexOf(key) !== -1) {
                                jsonld.r.setIRIs(resource, key,
                                    "http://plugins.linkedpipes.com/resource/configuration/Inherit");
                            }
                        }
                    });
                    //
                    statusService.success({
                        'title': 'Template created.'
                    });
                    $mdDialog.hide();
                });
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
            // The HierarchyTab list all parents of given template, but
            // as the new template is not yet created we can list only from
            // grandparents. A solution is to create a stub template.
            const parents = [];
            Array.prototype.push.apply(parents, $scope.component._parents)
            parents.push($scope.component);

            // We use shallow copy here as we know we will not modify it.
            const template = jQuery.extend(false, [],  $scope.component);
            template._parents = parents;

            // $scope.component

            $scope.api.store = {
                "template": template,
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

            $scope.infoLink = template._core.infoLink;

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
