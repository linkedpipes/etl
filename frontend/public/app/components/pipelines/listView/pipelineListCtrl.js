define([], function () {
    function controler($scope, $location, $http, $timeout, $mdDialog, $mdMedia,
                       refreshService, statusService, jsonldService, clipboard) {

        var template = {
            'iri': {
                '$property': '@id'
            },
            'label': {
                '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
            },
            'tags': {
                '$property': 'http://etl.linkedpipes.com/ontrology/tag',
                '$type': 'array'
            }
        };

        $scope.searchLabel = '';
        $scope.searchTags = [];

        $scope.search = {
            'tags': {
                'searchText': '',
                // List of al tags, for filtering we use integers as tag
                // indexes instead of strings. This value is also use
                // to help use with filling in tags.
                'all': []
            },
        };

        $scope.search.tags.querySearch = function (query) {
            return $scope.search.tags.all.filter(function (item) {
                return item.indexOf(query) !== -1;
            });
        };

        $scope.info = {
            'clipboard': clipboard.supported
        };

        $scope.repository = jsonldService.createRepository({
            'template': template,
            'query': {
                'data': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://linkedpipes.com/ontology/Pipeline'
                },
                'deleted': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://linkedpipes.com/ontology/Tombstone'
                }
            },
            'decorator': function (item) {
                item['searchLabel'] = item['label'].toLowerCase();
                // Show by default.
                item['show'] = true;
                item['filterLabel'] = true;
                item['filterTags'] = true;
                // The rest is only for pipelines with tags.
                if (!item['tags'] || item['tags'].length == 0) {
                    item['tags'] = [];
                    return;
                }
                // Get indexes of our tags.
                // var tagIndexes = [];
                item['tags'].forEach(function (tag) {
                    var tagIndex = $scope.search.tags.all.indexOf(tag);
                    if (tagIndex === -1) {
                        // tagIndex = $scope.search.tags.all.length;
                        $scope.search.tags.all.push(tag);
                    }
                    // tagIndexes.push(tagIndex);
                });
                // We use integers instead of strings.
                // tagIndexes.sort();
                // item['searchTags'] = tagIndexes;
            },
            'url': '/resources/pipelines'
        });

        $scope.onPipeline = function (pipeline) {
            $location.path('/pipelines/edit/canvas').search({
                'pipeline': pipeline.iri
            });
        };

        $scope.onExecute = function (pipeline) {
            $http.post('/resources/executions?pipeline=' + pipeline.iri)
                .then(function (response) {
                    $location.path('/executions').search({});
                }, function (response) {
                    statusService.postFailed({
                        'title': "Can't start the execution.",
                        'response': response
                    });
                });
        };

        $scope.onExport = function (pipeline, $event) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                'controller': 'components.pipelines.export.dialog',
                'templateUrl': 'app/components/pipelines/exportDialog/pipelineExportDialogView.html',
                'parent': angular.element(document.body),
                'targetEvent': $event,
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    'data': {
                        'iri': pipeline.iri,
                        'label': pipeline.label
                    }
                }
            });
        };

        $scope.onCreate = function () {
            var data = new FormData();
            // Use empty options.
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions'
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
            $http.post('/resources/pipelines/', data, config).then(
                function (response) {
                    // The response is a reference.
                    // TODO Use JSONLD service to get the value !!
                    console.log(response.data);
                    var newPipelineUri = response.data[0]['@graph'][0]['@id'];
                    //
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': newPipelineUri
                    });
                }, function (response) {
                    statusService.postFailed({
                        'title': "Can't create the pipeline.",
                        'response': response
                    });
                });
        };

        $scope.onUpload = function () {
            $location.path('/pipelines/upload').search({});
        };

        $scope.openMenu = function ($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
        };

        $scope.onCopy = function (pipeline) {
            var data = new FormData();
            //
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/import': true
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
            }
            var url = '/resources/pipelines?pipeline='
                + pipeline.iri;
            $http.post(url, data, config).then(function (response) {
                statusService.success({
                    'title': 'Pipeline has been successfully copied.'
                });
                // Force update.
                $scope.repository.update();
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't copy pipeline.",
                    'response': response
                });
            });
        };

        $scope.onCopyIri = function (pipeline) {
            clipboard.copyText(pipeline.iri);
        };

        $scope.onDelete = function (pipeline, event) {
            var confirm = $mdDialog.confirm()
                .title('Would you like to delete pipeline "'
                    + pipeline.label + '"?')
                .ariaLabel('Delete pipeline.')
                .targetEvent(event)
                .ok('Delete pipeline')
                .cancel('Cancel');
            $mdDialog.show(confirm).then(function () {
                $scope.repository.delete(pipeline);
                // TODO Do not force reload here, use update.
                $scope.repository.load();
            });
        };

        $scope.$watch('searchLabel', function (newValue, oldValue) {
            if (newValue === oldValue) {
                return;
            }
            // Special care for empty query.
            if (newValue === '') {
                $scope.repository.data.forEach(function (item) {
                    item['filterLabel'] = true;
                    item['show'] = item['filterLabel'] && item['filterTags'];
                });
                return;
            }
            //
            var query = newValue.toLowerCase();
            $scope.repository.data.forEach(function (item) {
                item['filterLabel'] = item['searchLabel'].indexOf(query) !== -1;
                item['show'] = item['filterLabel'] && item['filterTags'];
            });
        });

        $scope.chipsFilter = function () {
            // Special for no tags.
            if ($scope.searchTags.length === 0) {
                $scope.repository.data.forEach(function (item) {
                    item['filterTags'] = true;
                    item['show'] = item['filterLabel'] && item['filterTags'];
                });
                return;
            }
            $scope.repository.data.forEach(function (item) {
                if (item.tags.length < $scope.searchTags.length) {
                    item['filterTags'] = false;
                    item['show'] = item['filterLabel'] && item['filterTags'];
                    return;
                }
                item['filterTags'] = true;
                for (var index in $scope.searchTags) {
                    if (item.tags.indexOf($scope.searchTags[index]) === -1) {
                        item['filterTags'] = false;
                        break;
                    }
                }
                item['show'] = item['filterLabel'] && item['filterTags'];
            });
        };

        var initialize = function () {
            $scope.repository.load(function () {
                },
                function (response) {
                    statusService.getFailed({
                        'title': "Can't load data.",
                        'response': response
                    });
                });
            refreshService.set(function () {
                // TODO Enable update once the server has
                // proper support of the JSON-LD repository.
//                $scope.repository.update();
            });
        };
        $timeout(initialize, 0);
    }

    //
    controler.$inject = ['$scope', '$location', '$http', '$timeout',
        '$mdDialog', '$mdMedia', 'service.refresh',
        'services.status', 'services.jsonld', 'clipboard'];
    //
    function init(app) {
        app.controller('components.pipelines.list', controler);
    }

    return init;
});
