define([], function () {
    function controler($scope, $location, $http, $timeout, $mdDialog, $mdMedia,
            refreshService, repositoryService, statusService, jsonldService) {

        var template = {
            'iri': {
                '$property': 'http://linkedpipes.com/ontology/pipeline'
            },
            'id': {
                '$property': 'http://linkedpipes.com/ontology/id'
            },
            'label': {
                '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
            }
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
            'decorator': function () {},
            'url': '/resources/pipelines'
        });

        $scope.onPipeline = function (pipeline) {
            $location.path('/pipelines/edit/canvas').search({
                'pipeline': pipeline.iri
            });
        };

        $scope.onExecute = function (pipeline) {
            $http.post('/api/v1/execute?uri=' + pipeline.iri)
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
                    + pipeline.uri;
            $http.post(url).then(function (response) {
                statusService.success({
                    'title': 'Pipeline has been successfully copied.'
                });
                // Force update.
                repositoryService.get($scope.repository);
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't copy pipeline.",
                    'response': response
                });
            });
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

        var initialize = function () {
            $scope.repository.load(function () { },
                    function (response) {
                        statusService.getFailed({
                            'title': "Can't load data.",
                            'response': response
                        });
                    });

            refreshService.set(function () {
                // TOTO Update data here!
            });
        };
        $timeout(initialize, 0);
    }
    //
    controler.$inject = ['$scope', '$location', '$http', '$timeout',
        '$mdDialog', '$mdMedia', 'service.refresh', 'services.repository',
        'services.status', 'services.jsonld'];
    //
    function init(app) {
        app.controller('components.pipelines.list', controler);
    }
    return init;
});
