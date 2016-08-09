define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'fileName': '',
            'fileType': '',
            'graphUri': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-rdfToFile#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.fileType = rdf.getString(resource, 'fileType');
            $scope.dialog.graphUri = rdf.getString(resource, 'graphUri');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setString(resource, 'fileType', $scope.dialog.fileType);
            rdf.setString(resource, 'graphUri', $scope.dialog.graphUri);

            return rdf.getData();
        };

        // Define the save function.
        $service.onStore = function () {
            saveDialog();
        }

        // Load data.
        loadDialog();
    }
    //
    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
