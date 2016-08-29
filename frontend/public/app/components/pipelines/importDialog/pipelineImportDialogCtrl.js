define([], function () {
    function controler($scope, $mdDialog, $http,
                       statusService, jsonldService) {

        // Fragment sources.
        $scope.url = '';
        $scope.file = undefined;
        $scope.pipeline = '';
        $scope.pipelineLoaded = false;
        $scope.pipelineFilter = '';

        $scope.activeTab = 0;

        $scope.importing = false;

        var template = {
            'iri': {
                '$property': '@id'
            },
            'label': {
                '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
            },
            'selected': false
        };

        /**
         * Load and return pipeline from given IRI.
         */
        function loadFromIri(iri, fromLocal) {
            $scope.importing = true;
            //
            var data = new FormData();
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/local': fromLocal
            };
            data.append('options', new Blob([JSON.stringify(options)], {
                type: "application/ld+json"
            }), 'options.jsonld');
            //
            var config = {
                'transformRequest': angular.identity,
                'headers': {
                    // By this angular add Content-Type itself.
                    'Content-Type': undefined,
                    'accept': 'application/ld+json'
                }
            };
            //
            $http.post(iri, data, config).then(function (response) {
                $scope.importing = false;
                $mdDialog.hide({
                    'pipeline': response.data
                });
            }, function (response) {
                $scope.importing = false;
                statusService.getFailed({
                    'title': "Can't load the pipeline.",
                    'response': response
                });
            });
        }

        /**
         * Import from local file.
         * TODO: Also use server to update the pipeline.
         */
        function importFile() {
            // We need to localize (update) the given pipeline file.
            var data = new FormData();
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/local': false
            };
            data.append('options', new Blob([JSON.stringify(options)], {
                type: "application/ld+json"
            }), 'options.jsonld');
            // data.append('pipeline', new Blob([reader.result], {
            //     type: "application/ld+json"
            // }), $scope.file.name);
            data.append('pipeline', $scope.file);

            var config = {
                'transformRequest': angular.identity,
                'headers': {
                    // By this angular add Content-Type itself.
                    'Content-Type': undefined,
                    'accept': 'application/ld+json'
                }
            };

            $http.post('/resources/localize', data, config).then(
                function (response) {
                    $scope.importing = false;
                    $mdDialog.hide({
                        'pipeline': response.data
                    });
                }, function (response) {
                    $scope.importing = false;
                    statusService.getFailed({
                        'title': "Can't load the pipeline.",
                        'response': response
                    });
                });
        }

        $scope.repository = jsonldService.createRepository({
            'template': template,
            'query': {
                'data': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://linkedpipes.com/ontology/Pipeline'
                }
            },
            'decorator': function () {
            },
            'url': '/resources/pipelines'
        });

        /**
         * Given a pipeline item return true if pipeline should be visible
         * with current filter.
         */
        $scope.filterPipelines = function (item) {
            // Show all if there is no filter.
            if (!$scope.pipelineFilter || $scope.pipelineFilter === '') {
                return true;
            }
            // Always show selected item.
            if (item.iri === $scope.pipeline) {
                return true;
            }
            // Otherwise match text.
            var searchString = $scope.pipelineFilter.toLowerCase();
            return item.label.toLowerCase().indexOf(searchString) !== -1;
        };

        /**
         * Check on pipeline change, make sure that only one pipeline
         * can be selected at a time.
         */
        $scope.onPipelineChange = function (item) {
            if (item.selected) {
                $scope.repository.data.forEach(function (item) {
                    item.selected = false;
                });
                $scope.pipeline = item;
            } else {
                $scope.pipeline = undefined;
            }
            item.selected = true;

        };

        /**
         * Load list of local pipelines on the first opening of pipelines
         * tab.
         */
        $scope.onPipelineTab = function () {
            console.log('loading..');
            if ($scope.pipelineLoaded) {
                return;
            } else {
                // Load pipeline list.
                $scope.repository.load(function () {
                    $scope.pipelineLoaded = true;
                }, function (response) {
                    statusService.getFailed({
                        'title': "Can't load data.",
                        'response': response
                    });
                });
            }
        };

        $scope.onImport = function () {
            if ($scope.activeTab === 0) {
                // Import from IRI.
                loadFromIri('/resources/localize?pipeline=' + $scope.url, false);
            } else if ($scope.activeTab === 1) {
                // Import from a file.
                importFile();
            } else if ($scope.activeTab === 2) {
                // Import from IRI on local machine.
                if ($scope.pipeline === undefined || $scope.pipeline === '') {
                    // Do nothing as no pipeline is selected.
                    statusService.getFailed({
                        'title': "No pipeline selected for import."
                    });
                } else {
                    // Just get the local pipeline and return it.
                    $http.get($scope.pipeline).then(function (response) {
                        $scope.importing = false;
                        var pipeline = response.data;
                        $mdDialog.hide({
                            'pipeline': pipeline
                        });
                    }, function (response) {
                        $scope.importing = false;
                        statusService.getFailed({
                            'title': "Can't load the pipeline.",
                            'response': response
                        });
                    });
                }
            } else {
                console.error('Invalid active tab: ', $scope.activeTab);
            }
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

    }

    controler.$inject = ['$scope', '$mdDialog', '$http',
        'services.status', 'services.jsonld'];

    return function init(app) {
        app.controller('components.pipelines.import.dialog', controler);
    };

});


