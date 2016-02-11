define([], function () {
    function controler($scope, $http, $timeout, $location, Upload, statusService) {

        $scope.fileReady = false;
        $scope.uploading = false;
        $scope.log = '';
        $scope.progress = 0;

        /**
         * Create a new pipeline with given ID.
         *
         * @param id
         * @param onSucess Called if pipeline is sucesfully created, as parameter pipeline URI is given.
         */
        var createPipeline = function (id, onSucess) {
            $http.post('/resources/pipelines/' + id).then(function (response) {
                if (onSucess) {
                    onSucess(response.data.uri);
                }
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't create the pipeline.",
                    'response': response
                });
            });
        };

        /**
         * Upload pipeline definition from user given file to the pipeline with given URI.
         *
         * @param uri
         */
        var importFromFile = function (uri) {
            $scope.uploading = true;
            var file = $scope.file;

            // We sent the file in the exact form we have recieved it.
            file.upload = Upload.http({
                url: uri,
                method: 'PUT',
                fields: {'Content-Type': 'application/json'},
                data: file
            });

            file.upload.then(function (response) {
                console.log(response);
                $timeout(function () {
                    $location.path('/pipelines/edit/canvas').search({'uri': uri});
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
            if (!$scope.fileReady) {
                return;
            }
            var id = 'created-' + (new Date()).getTime();
            createPipeline(id, importFromFile);
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