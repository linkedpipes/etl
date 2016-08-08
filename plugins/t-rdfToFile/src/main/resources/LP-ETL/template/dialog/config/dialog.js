define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'fileName': '',
            'fileType': '',
            'graphUri': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-rdfToFile#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.fileType = rdf.getString(resource, 'fileType');
            $scope.dialog.graphUri = rdf.getString(resource, 'graphUri');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setString(resource, 'fileType', $scope.dialog.fileType);
            rdf.setString(resource, 'graphUri', $scope.dialog.graphUri);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});