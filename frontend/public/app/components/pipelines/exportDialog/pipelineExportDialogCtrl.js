define(['file-saver'], function (saveAs) {
    function controler($scope, $http, $mdDialog, data, jsonldService) {

        // Enable user to
        $scope.cancelled = false;

        $scope.label = data.label;

        $scope.options = {
            'removePrivateConfiguration': true
        };

        var jsonld = jsonldService.jsonld();

        var onFailure = function (response) {
            $scope.cancelled = true;
        };

        /**
         * If pipeline is not available in data.pipeline then download it.
         */
        var loadPipeline = function (onSuccess) {
            if (typeof (data.pipeline) !== 'undefined') {
                onSuccess();
                return;
            }
            // Load pipeline.
            $scope.waiting_text = 'Loading pipeline ...';
            $http.get(data.iri + "?templates=true&mappings=true").then(
                function (response) {
                    data.pipeline = response.data;
                    onSuccess();
                }, function (response) {
                    onFailure(response);
                });
        };

        /**
         * Search for components in the pipeline.
         */
        var parsePipeline = function (onSuccess) {
            console.time('parsePipeline');
            data.model = {
                'graphs': {},
                'components': [],
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
                if (resource['@type'] === undefined) {
                    // Ignore resources without type.
                    return;
                }
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') !== -1 ||
                    resource['@type'].indexOf('http://linkedpipes.com/ontology/Template') !== -1) {
                    data.model.components.push(resource);
                    var templateIri = jsonld.getReference(resource,
                        'http://linkedpipes.com/ontology/template');
                    data.model.templates[templateIri] = null;
                }
            });
            console.timeEnd('parsePipeline');
            onSuccess();
        };

        /**
         * Download templates definitions into data.model.templates and
         * store mapping into data.model.templateMapping.
         */
        var loadTemplates = function (onSuccess) {
            $scope.waiting_text = 'Loading templates ...';

            var loadTemplate = function (iri, onSuccess) {
                var downloadIri = '/resources/components/definition?iri='
                    + encodeURIComponent(iri);
                $http.get(downloadIri).then(function (response) {
                    data.model.templates[iri] = response.data;
                    onSuccess();
                }, function (response) {
                    onFailure(response);
                });
            };

            // We need to download all templates and then continue.
            var downloadedTotal = Object.keys(data.model.templates).length;
            console.log(downloadedTotal);

            // There are no templates.
            if (downloadedTotal == 0) {
                onSuccess();
            }

            /**
             * Called after every success download. When all download are done
             * call callback.
             */
            var whenFinished = function () {
                downloadedCounter += 1;
                if (downloadedCounter === downloadedTotal) {
                    // Now when the data are downloaded we need to
                    // identify JarTemplate for evey template. This
                    // mapping is stored in the data.model.templateMapping.
                    data.model.templateMapping = {};
                    for (var iri in data.model.templates) {
                        var template = data.model.templates[iri];
                        // Default mapping.
                        data.model.templateMapping[iri] = iri;
                        // Search for a reference.
                        jsonld.iterateObjects(template, function (resource) {
                            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') !== -1 ||
                                resource['@type'].indexOf('http://linkedpipes.com/ontology/Template') !== -1) {
                                data.model.templateMapping[iri] =
                                    jsonld.getReference(resource,
                                        'http://linkedpipes.com/ontology/template');
                            }
                        });
                    }
                    //
                    onSuccess();
                }
            };

            var downloadedCounter = 0;
            for (var iri in data.model.templates) {
                loadTemplate(iri, whenFinished);
            }

        };

        /**
         * Given template IRI use data.model.templateMapping to resolve
         * it to the JarTemplate.
         */
        var resolveTemplate = function (iri) {
            while (iri !== data.model.templateMapping[iri]) {
                iri = data.model.templateMapping[iri];
            }
            return iri;
        }

        /**
         * Remove private properties from the configuration.
         */
        var removePrivateConfiguration = function () {
            for (var index in data.model.components) {
                var component = data.model.components[index];
                // Get template and configuration graph.
                var configIri = jsonld.getReference(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
                var config = data.model.graphs[configIri];
                var templateIri = jsonld.getReference(component,
                    'http://linkedpipes.com/ontology/template');
                var template = data.model.templates[resolveTemplate(templateIri)];
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
            $scope.cancelled = true;
            $mdDialog.cancel();
        };

        (function load() {
            $scope.waiting = true;
            loadPipeline(function () {
                if ($scope.cancelled) {
                    return;
                }
                parsePipeline(function () {
                    if ($scope.cancelled) {
                        return;
                    }
                    loadTemplates(function () {
                        prepareForExport(data.pipeline);
                        if ($scope.cancelled) {
                            return;
                        }
                        saveAs(new Blob([JSON.stringify(data.pipeline, null, 2)],
                            {type: 'text/json'}),
                            data.label + '.jsonld');
                        $mdDialog.hide();
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
