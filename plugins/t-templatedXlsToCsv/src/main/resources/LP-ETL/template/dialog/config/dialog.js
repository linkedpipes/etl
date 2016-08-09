define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'prefix': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-templatedXlsToCsv#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.prefix = rdf.getString(resource, 'prefix');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'prefix', $scope.dialog.prefix);

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
