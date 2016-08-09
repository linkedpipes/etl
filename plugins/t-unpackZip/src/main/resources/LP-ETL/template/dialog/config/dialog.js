define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'usePrefix': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-unpackZip#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.usePrefix = rdf.getBoolean(resource, 'usePrefix');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'usePrefix', $scope.dialog.usePrefix);

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
