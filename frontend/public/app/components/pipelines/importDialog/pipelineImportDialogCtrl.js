define([], function () {
    function controler($scope, $mdDialog, $http, statusService) {

        $scope.type = 'URL';
        $scope.loading = false;

        $scope.onImport = function () {
            if ($scope.type === 'URL') {
                $scope.loading = true;
                var url = '/api/v1/proxy?url=' + $scope.url;
                $http.get(url).then(function (response) {
                    $scope.loading = false;
                    var pipeline = response.data;
                    $mdDialog.hide({
                        'pipeline': pipeline
                    });
                }, function (response) {
                    $scope.loading = false;
                    statusService.getFailed({
                        'title': "Can't load the pipeline.",
                        'response': response
                    });
                });
            } else {
                $mdDialog.hide({
                    'pipeline': JSON.parse($scope.text)
                });
            }
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

    }
    controler.$inject = ['$scope', '$mdDialog', '$http', 'services.status'];

    return function init(app) {
        app.controller('components.pipelines.import.dialog', controler);
    };

});


