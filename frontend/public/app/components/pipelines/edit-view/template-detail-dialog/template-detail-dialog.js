define([
    "jquery",
    "jsonld",
    "app/components/templates/detailDirective/templateDetailDirective",
    "app/components/templates/template-service"
], function (jquery, jsonld, detailDirective, templateService) {
    "use strict";

    const LP = {
        "template": "http://linkedpipes.com/ontology/template",
        "color": "http://linkedpipes.com/ontology/color",
        "configurationGraph": "http://linkedpipes.com/ontology/configurationGraph",
        "control": "http://plugins.linkedpipes.com/ontology/configuration/control"
    };

    const SKOS = {
        "prefLabel": "http://www.w3.org/2004/02/skos/core#prefLabel"
    };

    const DCTERMS = {
        "description": "http://purl.org/dc/terms/description"
    };

    function controller(
        $scope, $http, $mdDialog, $templates, $status,
        component, template, configuration) {

        $scope.api = {};

        $scope.templateToEdit = undefined;
        $scope.configuration = undefined;

        $scope.onSave = () => {
            // Update shared data.
            $scope.api.save();

            const promise = createTemplate(
                $http, $scope.templateToEdit,
                template,
                $scope.configuration,
                $templates);
            $mdDialog.hide(promise);
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        function initialize() {
            $scope.templateToEdit = {
                "id": "",
                "label": jsonld.r.getPlainString(component, SKOS.prefLabel),
                "description": jsonld.r.getPlainString(
                    component, DCTERMS.description),
                "color": jsonld.r.getPlainString(component, LP.color)
            };

            $scope.infoLink = template._coreReference.infoLink;

            // Construct a new temporary template object.
            $scope.component = jQuery.extend({}, template,
                {
                    "id": template.id,
                    "template": template.id,
                });

            $scope.configuration = jQuery.extend(true, [], configuration);
            if ($scope.configuration === undefined) {
                $templates.fetchNewConfig(template.id).then((config) => {
                    $scope.configuration = jQuery.extend(true, [], config);
                    initializeDirective();
                });
            } else {
                initializeDirective();
            }

        }

        /**
         * Must be called once all data are ready. It will initialize the
         * directive.
         */
        function initializeDirective() {
            // The HierarchyTab list all parents of given template, but
            // as the new template is not yet created we can list only from
            // grandparents. A solution is to create a stub template.
            const parents = [];
            Array.prototype.push.apply(parents, $scope.component._parents);
            parents.push($scope.component);

            // We use shallow copy here as we know we will not modify it.
            const template = jQuery.extend(false, [], $scope.component);
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

        $templates.load().then(initialize);
    }

    function createTemplate(
        $http, template, parent, configuration, $templates) {

        const resource = {
            "@type": ["http://linkedpipes.com/ontology/Template"]
        };
        jsonld.r.setStrings(resource, SKOS.prefLabel, template.label);
        jsonld.r.setStrings(resource, DCTERMS.description, template.description);
        jsonld.r.setIRIs(resource, LP.template, parent.id);
        if (template.color !== undefined) {
            jsonld.r.setStrings(resource, LP.color, template.color);
        }

        return postNewTemplate($http, resource, configuration)
            .then((response) => {
                return $templates.forceLoad().then(() => {
                    return {
                        "template": response.data[0]["@graph"][0]["@id"],
                        "configuration": configuration
                    };
                });
            });
    }

    function postNewTemplate($http, template, configuration) {
        const postConfiguration = {
            "transformRequest": angular.identity,
            "headers": {
                "Content-Type": undefined,
                "Accept": "application/ld+json"
            }
        };

        const form = new FormData();
        form.append("component",
            new Blob([JSON.stringify(template)], {
                "type": "application/ld+json"
            }), "component.jsonld");
        form.append("configuration",
            new Blob([JSON.stringify(configuration)], {
                "type": "application/ld+json"
            }), "configuration.jsonld");
        return $http.post("./resources/components", form, postConfiguration)
    }


    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        detailDirective(app);
        templateService(app);
        app.controller("template.detail.dialog", [
            "$scope", "$http", "$mdDialog",
            "template.service", "services.status",
            "component", "template", "configuration",
            controller]);
    };

});
