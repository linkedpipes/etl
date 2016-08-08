define([], function () {
    function controler($scope, $http, $timeout, $location, Upload, statusService) {

        $scope.fileReady = false;
        $scope.uploading = false;
        $scope.log = '';
        $scope.progress = 0;
        $scope.type = 'file';

        /**
         * Upload pipeline definition from user given file to the pipeline with given URI.
         *
         * @param uri
         */
        var updateFromFile = function (uri) {
            $scope.uploading = true;
            var file = $scope.file;

            // We sent the file in the exact form we have recieved it.
            file.upload = Upload.http({
                url: uri,
                method: 'PUT',
                data: file
            });

            file.upload.then(function (response) {
                console.log(response);
                $timeout(function () {
                    $location.path('/pipelines/edit/canvas').search({'pipeline': uri});
                });
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't upload definition to the created pipeline.",
                    'response': response
                });
            }, function(event) {
                console.log(event);
                var percentage = parseInt(100.0 * event.loaded / event.total);
                console.log(percentage);
                $scope.progress = percentage;
            });
        };

        /**
         * Create a new pipeline with given ID.
         *
         * @param id
         * @param onSucess Called if pipeline is sucesfully created, as parameter pipeline URI is given.
         */
        var importFile = function () {
            var id = 'created-' + (new Date()).getTime();
            $http.post('/resources/pipelines/' + id).then(function (response) {
                updateFromFile(response.data.uri);
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't import the pipeline.",
                    'response': response
                });
            });
        };

        /**
         * Import pipeline from URI.
         *
         * @returns
         */
        var importUrl = function() {
            var id = 'created-' + (new Date()).getTime();
            var uri = '/resources/pipelines/' + id + '?pipeline=' + $scope.url;
            $http.post(uri).then(function (response) {
                $location.path('/pipelines/edit/canvas').search({'pipeline': response.data.uri});
            }, function (response) {
                console.log('failed:', response);
                statusService.postFailed({
                    'title': "Can't copy pipeline.",
                    'response': response
                });
            });

        };

        $scope.$watch('file', function () {
            if (!$scope.file) {
                $scope.fileReady = false;
                return;
            }
            if (!$scope.file.$error) {
                $scope.fileReady = true;
                console.log($scope.file);
            } else {
                // TODO Show error ..
                $scope.fileReady = false;
                console.log($scope.file);
            }
        });

        $scope.onUpload = function() {
            if ($scope.type === 'file') {
                if (!$scope.fileReady) {
                    return;
                }
                importFile(updateFromFile);
            } else if ($scope.type === 'url') {
                importUrl();
            } else {
                console.log('Unknown type.');
            }
        };

    }
    //
    controler.$inject = ['$scope', '$http', '$timeout', '$location', 'Upload', 'services.status'];
    //
    function init(app) {
        app.controller('components.pipelines.upload', controler);
    }
    return init;
});
