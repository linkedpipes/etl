define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'skipOnError': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-fileDecode#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.skipOnError = rdf.getBoolean(resource, 'skipOnError');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'skipOnError', $scope.dialog.skipOnError);

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
