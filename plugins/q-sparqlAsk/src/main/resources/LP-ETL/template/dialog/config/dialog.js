define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'query': '',
            'failOnTrue': true
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/q-sparqlAsk#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.query = rdf.getString(resource, 'query');
            $scope.dialog.failOnTrue = rdf.getBoolean(resource, 'failOnTrue');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'query', $scope.dialog.query);
            rdf.setBoolean(resource, 'failOnTrue', $scope.dialog.failOnTrue);

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
