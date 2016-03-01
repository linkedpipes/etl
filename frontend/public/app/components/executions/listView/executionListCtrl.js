define([
], function () {
    function controler($scope, $location, $timeout, $http, refreshService, repositoryService, statusService) {

        var listDecorator = function (item) {
            if (item.pipelineProgress !== null) {
                item.pipelineProgress.value = 100 *
                        (item.pipelineProgress.current / item.pipelineProgress.total);
                if (item.currentComponentProgress !== null) {
                    item.currentComponentProgress.value = 100 *
                            (item.currentComponentProgress.current / item.currentComponentProgress.total);
                }
            }
            //
            item.waitForDelete = false;
            //
            switch (item.statusCode) {
                case 120: // QUEUED
                    item.icon = {
                        'name': 'hourglass',
                        'style': {
                            'color': 'black'
                        }
                    };
                    item.view = 'QUEUED';
                    break;
                case 140: // INITIALIZING
                case 160: // RUNNING
                    item.icon = {
                        'name': 'run',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    item.view = 'RUNNNING';
                    break;
                case 200: // FINISHED
                    item.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'green'
                        }
                    };
                    item.view = 'FINISHED';
                    break;
                case 513: // INITIALIZATION_FAILED
                case 511: // FAILED
                case 512: // FAILED_ON_THROWABLE
                    item.icon = {
                        'name': 'error',
                        'style': {
                            'color': 'red'
                        }
                    };
                    item.view = 'FAILED';
                    break;
                default: // UNKNOWN
                    item.view = '';
                    break;
            }
            // Compute duration.
            if (item.end) {
                var duration = (item.end - item.start) / 1000;
                var durationSeconds = Math.ceil((duration) % 60);
                var durationMinutes = Math.floor((duration / (60)) % 60);
                var durationHours = Math.floor(duration / (60 * 60));
                item.duration = (durationHours < 10 ? '0' + durationHours : durationHours) +
                        ':' + (durationMinutes < 10 ? '0' + durationMinutes : durationMinutes) +
                        ':' + (durationSeconds < 10 ? '0' + durationSeconds : durationSeconds);
            } else {
                item.duration = '';
            }
        };

        var listOnDelete = function (item) {
            item.waitForDelete = true;
        };

        $scope.repository = repositoryService.createRepository({
            'uri': '/resources/executions',
            'updateOperation': listDecorator,
            'deleteOperation': listOnDelete
        });

        $scope.onExecution = function (execution) {
            $location.path('/executions/detail').search({'uri': execution.uri});
        };

        $scope.onPipeline = function (execution) {
            $location.path('/pipelines/edit/canvas').search({'pipeline': execution.pipelineUri, 'execution': execution.uri});
        };

        $scope.onExecute = function (execution) {
            $http.post('/api/v1/execute?uri=' + execution.pipelineUri).then(function (response) {
                repositoryService.update($scope.repository);
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't start the execution.",
                    'response': response
                });
            });
        };

        $scope.onDelete = function (execution) {
            repositoryService.delete($scope.repository, execution.id);
        };

        $scope.openMenu = function ($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
        };

        var initialize = function () {
            repositoryService.get($scope.repository, function () {
            }, function (response) {
                statusService.getFailed({
                    'title': "Can't load data.",
                    'response': response
                });
            });
            refreshService.set(function () {
                repositoryService.update($scope.repository);
            });
        };

        $timeout(initialize, 0);
    }
    //
    controler.$inject = ['$scope', '$location', '$timeout', '$http', 'service.refresh', 'services.repository', 'services.status'];
    //
    function init(app) {
        // refreshService
        // repositoryService
        app.controller('components.executions.list', controler);
    }
    return init;
});