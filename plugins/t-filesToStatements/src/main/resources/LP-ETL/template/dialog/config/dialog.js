define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'predicate': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesToStatements#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.predicate = rdf.getString(resource, 'predicate');

        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'predicate', $scope.dialog.predicate);

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
