define([], function () {
    function controler($scope, $location, $http, $timeout, $mdDialog, $mdMedia,
                       refreshService, statusService, jsonldService, clipboard) {

        var template = {
            'iri': {
                '$property': 'http://linkedpipes.com/ontology/pipeline'
            },
            'id': {
                '$property': 'http://linkedpipes.com/ontology/id'
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
                    'value': 'http://etl.linkedpipes.com/ontology/Reference'
                },
                'deleted': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Deleted'
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
            // Create pipeline.
            var id = $scope.id = 'created-' + (new Date()).getTime();
            $http.post('/resources/pipelines/' + id).then(function (response) {
                $location.path('/pipelines/edit/canvas').search({
                    'pipeline': response.data.uri
                });
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't create the pipeline.",
                    'response': response
                });
            });
            // TODO We may try a few time here, although the chance that
            // two users click in the exactly same unix time is rather small.
        };

        $scope.onUpload = function () {
            $location.path('/pipelines/upload').search({});
        };

        $scope.openMenu = function ($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
        };

        $scope.onCopy = function (pipeline) {
            var id = 'created-' + (new Date()).getTime();
            var url = '/resources/pipelines/' + id + '?pipeline='
                + pipeline.iri;
            $http.post(url).then(function (response) {
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
