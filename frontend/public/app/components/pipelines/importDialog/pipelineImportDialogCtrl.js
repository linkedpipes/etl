define([], function () {
    function controler($scope, $mdDialog, $http,
            statusService, jsonldService) {

        $scope.url = '';
        $scope.text = '';
        $scope.pipeline = undefined;
        $scope.pipelineLoaded = false;
        $scope.pipelineFilter = '';

        $scope.activeTab = 0;

        $scope.importing = false;


        var template = {
            'iri': {
                '$property': 'http://linkedpipes.com/ontology/pipeline'
            },
            'label': {
                '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
            },
            'selected': false
        };

        /**
         * Load and return pipeline from given IRI.
         */
        function loadFromIri(iri) {
            $scope.importing = true;
            $http.get(iri).then(function (response) {
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


        $scope.repository = jsonldService.createRepository({
            'template': template,
            'query': {
                'data': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Reference'
                }
            },
            'decorator': function () {},
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
            if (item.selected === true) {
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
                loadFromIri('/api/v1/proxy?url=' + $scope.url);
            } else if ($scope.activeTab === 1) {
                // Direct input.
                $mdDialog.hide({
                    'pipeline': JSON.parse($scope.text)
                });
            } else if ($scope.activeTab === 2) {
                // Import from IRI on local machine.
                if ($scope.pipeline === undefined) {
                    // Do nothing as no pipeline is selected.
                    statusService.getFailed({
                        'title': "No pipeline selected for import."
                    });
                } else {
                    loadFromIri($scope.pipeline.iri);
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


