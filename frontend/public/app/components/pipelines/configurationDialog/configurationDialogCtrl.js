/**
 * Dialog used to display the detail of a component.
 */
define(['app/components/componentDetailDirective/lpComponentDetailDirective'],
    function (componentDirective) {

        function controller($scope, $mdDialog, component, template, data,
                            jsonldService) {

            // Prepare data for this dialog.
            var JSONLD = jsonldService.jsonld();
            $scope.general = {
                'label': JSONLD.getString(component,
                    'http://www.w3.org/2004/02/skos/core#prefLabel'),
            };

            // Prepare data for lp-component-detail.
            $scope.api = {
                'show': function (name) {
                    return name === 'config' || name === 'instance';
                }
            };
            $scope.instance = component;
            $scope.template = template;
            prepareConfigurations();

            function prepareConfigurations() {
                // Rename property.
                var pipeline = data;

                // Load template configuration - there is only one graph.
                $scope.templateConfig = template['configuration'][0]['@graph'];

                // Load instance configuration.
                var instanceIri = JSONLD.getReference(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                if (instanceIri !== undefined) {
                    $scope.instanceConfig = pipeline.model['graphs'][instanceIri];
                } else {
                    // We use a COPY of the instance template.
                    $scope.instanceConfig = jQuery.extend(true, [],
                        $scope.templateConfig);
                }
            }

            $scope.onSave = function () {
                $scope.api.onSave();
                // Save configuration.
                var configGraph = JSONLD.getReference(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                if (configGraph === undefined) {
                    configGraph = component['@id'] + '/configuration';
                    // And also set the IRI to component.
                    component['http://linkedpipes.com/ontology/configurationGraph'] = {
                        '@id': configGraph
                    };
                }
                data.model['graphs'][configGraph] = $scope.instanceConfig;
                //
                $mdDialog.hide();
            };

            $scope.onCancel = function () {
                $mdDialog.cancel();
            };

        }

        controller.$inject = ['$scope', '$mdDialog', 'component',
            'template', 'data', 'services.jsonld'];

        return (app) => {
            componentDirective(app);
            app.controller('components.pipelines.configuration.dialog', controller);
        }

    });
