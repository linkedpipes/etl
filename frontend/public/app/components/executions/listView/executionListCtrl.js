define([
], function () {
    function controler($scope, $location, $timeout, $http, refreshService,
             statusService, jsonldService) {

        var template = {
            'iri': {
                '$resource': ''
            },
            'start': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/start'
            },
            'end': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/end'
            },
            'status': {
                '$property': 'http://etl.linkedpipes.com/ontology/status'
            },
            'status-monitor': {
                '$property': 'http://etl.linkedpipes.com/ontology/statusMonitor'
            },
            'size': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/size'
            },
            'progress': {
                'current': {
                    '$property': 'http://etl.linkedpipes.com/ontology/execution/componentFinished'
                },
                'total': {
                    '$property': 'http://etl.linkedpipes.com/ontology/execution/componentCount'
                }
            },
            'pipeline': {
                'iri': {
                    '$property': 'http://etl.linkedpipes.com/ontology/pipeline'
                },
                '_pipeline': {
                    '$property': 'http://etl.linkedpipes.com/ontology/pipeline',
                    '$oneToOne': {
                        'labels': {
                            '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel',
                            '$type': 'string'
                        }
                    }
                }
            }
        };

        var decorator = function (execution) {
            //
            execution.id = execution.iri.substring(
                    execution.iri.lastIndexOf('executions/') + 11);
            // Convert times.
            execution.startTime = Date.parse(execution.start);
            if (execution.end) {
                execution.endTime = Date.parse(execution.end);
            }
            // Get label.
            if (execution.pipeline.labels) {
                if (execution.pipeline.labels['en']) {
                    execution.label = execution.pipeline.labels['en'];
                } else if (execution.pipeline.labels['']) {
                    execution.label = execution.pipeline.labels[''];
                } else {
                    // TODO Use any other.
                }
            } else {
                execution.label = execution.id;
            }
            // Compute duration.
            if (execution.endTime) {
                var duration = (execution.endTime - execution.startTime) / 1000;
                var seconds = Math.ceil((duration) % 60);
                var minutes = Math.floor((duration / (60)) % 60);
                var hours = Math.floor(duration / (60 * 60));
                execution.duration = (hours < 10 ? '0' + hours : hours) +
                        ':' + (minutes < 10 ? '0' + minutes : minutes) +
                        ':' + (seconds < 10 ? '0' + seconds : seconds);
            } else {
                execution.duration = '';
            }
            //
            if (execution.progress) {
                execution.progress.value = 100 *
                        (execution.progress.current / execution.progress.total);
            }
            // Determine detail and icon type.
            switch (execution.status) {
                case 'http://etl.linkedpipes.com/resources/status/queued':
                    execution.canDelete = true;
                    execution.icon = {
                        'name': 'hourglass',
                        'style': {
                            'color': 'black'
                        }
                    };
                    execution.detailType = 'NONE';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    execution.canDelete = false;
                    execution.icon = {
                        'name': 'run',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    execution.detailType = 'PROGRESS';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    execution.canDelete = true;
                    execution.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'green'
                        }
                    };
                    execution.detailType = 'FULL';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    execution.canDelete = true;
                    execution.icon = {
                        'name': 'error',
                        'style': {
                            'color': 'red'
                        }
                    };
                    execution.detailType = 'FULL';
                    break;
                default:
                    execution.detailType = 'NONE';
                    break;
            }
        };

        $scope.repository = jsonldService.createRepository({
            'template': template,
            'query': {
                'data': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Execution'
                },
                'deleted': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Deleted'
                }
            },
            'decorator': decorator,
            'url': '/resources/executions'
        });

        $scope.onExecution = function (execution) {
            $location.path('/executions/detail').search({
                'execution': execution.iri
            });
        };

        $scope.onPipeline = function (execution) {
            $location.path('/pipelines/edit/canvas').search({
                'pipeline': execution.pipeline.iri,
                'execution': execution.iri});
        };

        $scope.onExecute = function (execution) {
            $http.post('/api/v1/execute?uri=' + execution.pipeline.iri)
                    .then(function () {
                        $scope.repository.update();
                    }, function (response) {
                        statusService.postFailed({
                            'title': "Can't start the execution.",
                            'response': response
                        });
                    });
        };

        $scope.onDelete = function (execution) {
            $scope.repository.delete(execution);
        };

        $scope.openMenu = function ($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
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
                $scope.repository.update();
            });
        };

        $timeout(initialize, 0);
    }
    //
    controler.$inject = ['$scope', '$location', '$timeout', '$http',
        'service.refresh', 'services.status',
        'services.jsonld'];
    //
    function init(app) {
        // refreshService
        // repositoryService
        app.controller('components.executions.list', controler);
    }
    return init;
});