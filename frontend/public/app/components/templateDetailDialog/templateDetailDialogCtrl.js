/**
 * Dialog used to display the detail of a component.
 */
define(['jquery', 'app/components/componentDetailDirective/lpComponentDetailDirective'],
    function (jQuery, componentDirective) {

        /**
         * Create new template.
         *
         * @param $http
         * @param template
         * @param configuration
         * @param onSuccess Callback with component IRI.
         * @param onFailed
         */
        function createTemplate($http, template, configuration, onSuccess, onFailed) {

            // Create new template object.
            var newTemplate = {
                '@type' : ['http://linkedpipes.com/ontology/Template'],
                'http://www.w3.org/2004/02/skos/core#prefLabel':
                    template['http://www.w3.org/2004/02/skos/core#prefLabel'],
                'http://purl.org/dc/terms/description':
                    template['http://purl.org/dc/terms/description'],
                'http://linkedpipes.com/ontology/template':
                    template['http://linkedpipes.com/ontology/template']
            };

            if (template['http://linkedpipes.com/ontology/color'] !== undefined) {
                newTemplate['http://linkedpipes.com/ontology/color'] =
                    template['http://linkedpipes.com/ontology/color'];
            }

            // Prepare POST request.

            var config = {
                'transformRequest': angular.identity,
                'headers': {
                    'Content-Type': undefined
                }
            }

            var data = new FormData();

            data.append('component', new Blob([JSON.stringify(newTemplate)], {
                type: "application/ld+json"
            }), 'component.jsonld');

            data.append('configuration', new Blob([JSON.stringify(configuration)], {
                type: "application/ld+json"
            }), 'configuration.jsonld');

            $http.post('./resources/components', data, config)
                .success(function (data, status, headers) {
                    console.log('Response:', data)
                   onSuccess();
                })
                .error(function (data, status, headers) {
                    onFailed(data);
                });
        }

        function controller($scope, $http, $mdDialog, component, template, pipeline,
                            jsonldService, statusService) {

            // Prepare data for this dialog.
            var JSONLD = jsonldService.jsonld();
            $scope.general = {
                'label': JSONLD.getString(component,
                    'http://www.w3.org/2004/02/skos/core#prefLabel'),
            };

            // Prepare data for lp-component-detail.
            $scope.api = {
                'show': function (name) {
                    return name === 'config' || name === 'template';
                }
            };
            // We use copy of the instance resource.
            $scope.instance = jQuery.extend(true, {}, component);
            $scope.template = template;
            prepareConfigurations();

            function prepareConfigurations() {

                // Load template configuration.
                $scope.templateConfig = template['configuration'][0]['@graph'];

                // Load instance configuration.
                var instanceIri = JSONLD.getReference(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                if (instanceIri !== undefined) {
                    $scope.instanceConfig = jQuery.extend(true, [],
                        pipeline.model['graphs'][instanceIri]);
                } else {
                    // We use a COPY of the instance template.
                    $scope.instanceConfig = jQuery.extend(true, [],
                        $scope.templateConfig);
                }
            }

            $scope.onSave = function () {
                $scope.api.onSave();
                // Now there is much to do ...
                createTemplate($http, $scope.instance, $scope.instanceConfig, function () {

                        // Update component : change parent IRI
                        // and disconnect configuration as it was
                        // 'moved' to a template.

                        statusService.success({
                            'title': 'Template created.'
                        });

                        // $mdDialog.hide();

                    }, function (response) {
                        // Can't load ..
                        statusService.postFailed({
                            'title': "Can't create template.",
                            'response': response
                        });
                    })
            };

            $scope.onCancel = function () {
                $mdDialog.cancel();
            };

        }

        controller.$inject = ['$scope', '$http', '$mdDialog', 'component',
            'template', 'pipeline', 'services.jsonld', 'services.status'];

        return (app) => {
            componentDirective(app);
            app.controller('components.templates.configuration.dialog', controller);
        }

    });
