define([], function () {
    function controler($scope, $location, $http, $timeout, refreshService, repositoryService,
            statusService) {

        var listDecorator = function (item) {
            item.waitForDelete = false;
        };

        var listOnDelete = function (item) {
            item.waitForDelete = true;
        };

        $scope.repository = repositoryService.createRepository({
            'uri': '/resources/pipelines',
            'updateOperation': listDecorator,
            'deleteOperation': listOnDelete
        });

        $scope.onPipeline = function (pipeline) {
            $location.path('/pipelines/edit/canvas').search({'pipeline': pipeline.uri});
        };

        $scope.onExecute = function (pipeline) {
            $http.post('/api/v1/execute?uri=' + pipeline.uri).then(function (response) {
                $location.path('/executions').search({});
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't start the execution.",
                    'response': response
                });
            });
        };

        $scope.onCreate = function () {
            // Create pipeline.
            var id = $scope.id = 'created-' + (new Date()).getTime();
            $http.post('/resources/pipelines/' + id).then(function (response) {
                $location.path('/pipelines/edit/canvas').search({'pipeline': response.data.uri});
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't create the pipeline.",
                    'response': response
                });
            });
            // TODO We may try a few time here, although the chance that two users click in the exactly same unix time
            // is rather small.
        };

        $scope.onUpload = function () {
            $location.path('/pipelines/upload').search({});
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
                // TODO Enable update !
                //repositoryService.update($scope.repository);
            });
        };
        $timeout(initialize, 0);
    }
    //
    controler.$inject = ['$scope', '$location', '$http', '$timeout', 'service.refresh',
        'services.repository', 'services.status'];
    //
    function init(app) {
        app.controller('components.pipelines.list', controler);
    }
    return init;
});