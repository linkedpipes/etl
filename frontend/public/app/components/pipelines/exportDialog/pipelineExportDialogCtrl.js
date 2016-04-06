/**
 * Given 'data' must contains IRI of a pipeline to export or
 * loaded pipeline.
 */
define(['file-saver'], function (saveAs) {
    function controler($scope, $http, $mdDialog, data, jsonldService) {

        // Enable user to
        $scope.waiting = false;

        $scope.label = data.label;

        $scope.options = {
            'removePrivateConfiguration': true
        };

        var jsonld = jsonldService.jsonld();

        var onFailure = function (response) {

        };

        /**
         * If pipeline is not available in data.pipeline then download it.
         */
        var loadPipeline = function (onSucess) {
            if (typeof (data.pipeline) !== 'undefined') {
                onSucess();
            }
            // Load pipeline.
            $scope.waiting_text = 'Loading pipeline ...';
            $http.get(data.iri).then(function (response) {
                data.pipeline = response.data;
                onSucess();
            }, function (response) {
                onFailure(response);
            });
        };

        /**
         * Search for componenets, configurations in the pipeline.
         */
        var parsePipeline = function (onSucess) {
            console.time('parsePipeline');
            data.model = {
                'graphs': {},
                'componenets': [],
                /**
                 * Key is the template IRI value is null as the template
                 * is not yet loaded.
                 */
                'templates': {}
            };

            jsonld.iterateGraphs(data.pipeline, function (graph, graph_iri) {
                data.model.graphs[graph_iri] = graph;
            });

            jsonld.iterateObjects(data.pipeline, function (resource) {
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') !== -1) {
                    data.model.componenets.push(resource);
                    var templateIri = jsonld.getReference(resource,
                            'http://linkedpipes.com/ontology/template');
                    data.model.templates[templateIri] = null;
                }
            });
            console.timeEnd('parsePipeline');
            onSucess();
        };

        /**
         * Download templates definitions into data.model.templates.
         */
        var loadTemplates = function (onSucess) {
            $scope.waiting_text = 'Loading templates ...';

            var loadTemplate = function (iri, onSucess) {
                $http.get(iri).then(function (response) {
                    data.model.templates[iri] = response.data;
                    onSucess();
                }, function (response) {
                    onFailure(response);
                });
            };

            // We need to download all templates and then continue.

            var downloadedTotal = 0;
            for (var iri in data.model.templates) {
                downloadedTotal += 1;
            }

            /**
             * Called after every sucess download. When all download are done
             * call callback.
             */
            var whenFinished = function () {
                downloadedCounter += 1;
                if (downloadedCounter === downloadedTotal) {
                    onSucess();
                }
            };

            var downloadedCounter = 0;
            for (var iri in data.model.templates) {
                loadTemplate(iri, whenFinished);
            }

        };

        /**
         * Remove private properties from the configuration.
         */
        var removePrivateConfiguration = function () {
            for (var index in data.model.componenets) {
                var component = data.model.componenets[index];
                // Get template and configuration graph.
                var configIri = jsonld.getReference(component,
                        'http://linkedpipes.com/ontology/configurationGraph');
                var config = data.model.graphs[configIri];
                var templateIri = jsonld.getReference(component,
                        'http://linkedpipes.com/ontology/template');
                var template = data.model.templates[templateIri];
                // Get properties to update.
                var privateProperties;
                jsonld.iterateObjects(template, function (resource) {
                    var type = resource['@type'];
                    if (type.indexOf('http://linkedpipes.com/ontology/ConfigurationDescription') !== -1) {
                        privateProperties = jsonld.getReferenceAll(resource,
                                'http://linkedpipes.com/ontology/privateProperties');
                    }
                });
                if (!privateProperties || typeof (config) === 'undefined') {
                    continue;
                }
                // Update.
                config.forEach(function (resource) {
                    for (var index in privateProperties) {
                        var predicate = privateProperties[index];
                        delete resource[predicate];
                    }
                });

            }
        };

        /**
         * Prepare data.pipeline for download.
         */
        var prepareForExport = function () {
            console.time('prepareForExport');
            if ($scope.options.removePrivateConfiguration) {
                removePrivateConfiguration();
            }
            console.timeEnd('prepareForExport');
        };

        $scope.onClose = function () {
            // TODO Cancel loading ..
            $mdDialog.cancel();
        };

        $scope.onExport = function () {
            prepareForExport(data.pipeline);
            saveAs(new Blob([JSON.stringify(data.pipeline, null, 2)],
                    {type: 'text/json'}),
                    data.label + '.jsonld');
            $mdDialog.hide();
        };

        (function load() {
            $scope.waiting = true;
            loadPipeline(function () {
                parsePipeline(function () {
                    loadTemplates(function () {
                        $scope.waiting = false;
                    });
                });
            });
        })();

    }

    controler.$inject = ['$scope', '$http', '$mdDialog', 'data',
        'services.jsonld'];

    return function init(app) {
        app.controller('components.pipelines.export.dialog', controler);
    };

});
