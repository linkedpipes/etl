define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'commitSize': '',
            'mimeType': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesToRdf#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.commitSize = rdf.getInteger(resource, 'commitSize');
            $scope.dialog.mimeType = rdf.getString(resource, 'mimeType');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setInteger(resource, 'commitSize', $scope.dialog.commitSize);
            rdf.setString(resource, 'mimeType', $scope.dialog.mimeType);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});