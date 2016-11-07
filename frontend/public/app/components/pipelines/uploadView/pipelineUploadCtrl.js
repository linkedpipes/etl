define([], function () {
    function controler($scope, $http, $timeout, $location, Upload,
                       statusService, templateService) {

        $scope.fileReady = false;
        $scope.uploading = false;
        $scope.log = '';
        $scope.type = 'file';

        $scope.importTemplates = true;
        $scope.updateTemplates = false;

        /**
         * Create a new pipeline with given ID.
         *
         * @param id
         * @param onSucess Called if pipeline is sucesfully created, as parameter pipeline URI is given.
         */
        var importFile = function () {
            $scope.uploading = true;
            var data = new FormData();
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/local': 'false',
                'http://etl.linkedpipes.com/ontology/importTemplates': $scope.importTemplates,
                'http://etl.linkedpipes.com/ontology/updateTemplates': $scope.updateTemplates
            };
            data.append('options', new Blob([JSON.stringify(options)], {
                type: "application/ld+json"
            }), 'options.jsonld');
            data.append('pipeline', $scope.file);
            //
            var config = {
                'transformRequest': angular.identity,
                'headers': {
                    // By this angular add Content-Type itself.
                    'Content-Type': undefined,
                    'accept': 'application/ld+json'
                }
            };
            var uri = '/resources/pipelines';
            $http.post(uri, data, config).then(function(response) {
                templateService.load(true).then(() => {
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': response.data[0]['@graph'][0]['@id']
                    });
                }, () => {
                    statusService.getFailed({
                        'title': "Can't update templates.",
                        'response': response
                    });
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': response.data[0]['@graph'][0]['@id']
                    });
                });
            }, function (response) {
                console.log('failed:', response);
                statusService.postFailed({
                    'title': "Can't copy pipeline.",
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
            var data = new FormData();
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/local' : 'false',
                'http://etl.linkedpipes.com/ontology/importTemplates': $scope.importTemplates,
                'http://etl.linkedpipes.com/ontology/updateTemplates': $scope.updateTemplates
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
            var uri = '/resources/pipelines?pipeline=' +
                encodeURIComponent($scope.url);
            $http.post(uri, data, config).then( function(response) {
                templateService.load(true).then(() => {
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': response.data[0]['@graph'][0]['@id']
                    });
                }, () => {
                    statusService.getFailed({
                        'title': "Can't update templates.",
                        'response': response
                    });
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': response.data[0]['@graph'][0]['@id']
                    });
                });
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
            $scope.uploading = true;
            if ($scope.type === 'file') {
                if (!$scope.fileReady) {
                    return;
                }
                importFile();
            } else if ($scope.type === 'url') {
                importUrl();
            } else {
                console.log('Unknown type.');
            }
        };

    }
    //
    controler.$inject = ['$scope', '$http', '$timeout', '$location', 'Upload',
        'services.status', 'template.service'];
    //
    function init(app) {
        app.controller('components.pipelines.upload', controler);
    }
    return init;
});
