define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'fileUri': '',
            'fileName': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-httpGetFile#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.uri = rdf.getString(resource, 'fileUri');
            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileUri', $scope.dialog.uri);
            rdf.setString(resource, 'fileName', $scope.dialog.fileName);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});