define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'query': '',
            'fileName': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-sparqlSelect#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.query = rdf.getString(resource, 'query');
            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'query', $scope.dialog.query);
            rdf.setString(resource, 'fileName', $scope.dialog.fileName);

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
