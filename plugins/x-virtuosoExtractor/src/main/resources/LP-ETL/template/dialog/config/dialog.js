define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'host': '',
            'userName': '',
            'password': '',
            'outputPath': '',
            'graph' : ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/x-virtuosoExtractor#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.host = rdf.getString(resource, 'uri');
            $scope.dialog.userName = rdf.getString(resource, 'username');
            $scope.dialog.password = rdf.getString(resource, 'password');
            $scope.dialog.outputPath = rdf.getString(resource, 'outputPath');
            $scope.dialog.graph = rdf.getString(resource, 'graph');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'uri', $scope.dialog.host);
            rdf.setString(resource, 'username', $scope.dialog.userName);
            rdf.setString(resource, 'password', $scope.dialog.password);
            rdf.setString(resource, 'outputPath', $scope.dialog.outputPath);
            rdf.setString(resource, 'graph', $scope.dialog.graph);

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
