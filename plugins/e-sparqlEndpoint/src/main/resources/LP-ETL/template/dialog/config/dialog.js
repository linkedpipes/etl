define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'query': '',
            'endpoint': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.query = rdf.getString(resource, 'query');
            $scope.dialog.endpoint = rdf.getString(resource, 'endpoint');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'query', $scope.dialog.query);
            rdf.setString(resource, 'endpoint', $scope.dialog.endpoint);

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
