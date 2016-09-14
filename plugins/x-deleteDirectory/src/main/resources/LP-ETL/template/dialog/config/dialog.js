define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'deleteDirectory': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/x-deleteDirectory#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.deleteDirectory = rdf.getString(resource, 'deleteDirectory');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'deleteDirectory', $scope.dialog.deleteDirectory);

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
